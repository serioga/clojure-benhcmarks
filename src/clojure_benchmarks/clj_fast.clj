(ns clojure-benchmarks.clj-fast
  (:import
    (java.util HashMap Map)))

(set! *warn-on-reflection* true)

;;; Credit Metosin
;;; https://github.com/metosin/reitit/blob/0bcfda755f139d14cf4eff37e2b294f573215213/modules/reitit-core/src/reitit/impl.cljc#L136
(defn fast-assoc
  [a k v]
  (let [a (or a {})]
    (.assoc ^clojure.lang.Associative a k v)))


;;; Credit Metosin
;;; https://github.com/metosin/reitit/blob/0bcfda755f139d14cf4eff37e2b294f573215213/modules/reitit-core/src/reitit/impl.cljc#L136
(defn fast-map [m]
  (let [m (or m {})]
    (HashMap. ^Map m)))

;;; Credit Metosin
(defn fast-get
  [^HashMap m k]
  (.get m k))


;;; Credit Metosin
;;; https://github.com/metosin/compojure-api/blob/master/src/compojure/api/common.clj#L46
(defn fast-map-merge
  [x y]
  (reduce-kv fast-assoc (or x {}) y))
(defn fast-map-merge2
  [x y]
  (reduce-kv assoc (or x {}) y))

(defn merge2
  "Merge two hash-maps `m1` and `m2`.
   Skip merge if `m2` is empty."
  [m1 m2]
  (case (count m2)
    0 m1
    1 (conj m1 (first m2))
    (reduce conj (or m1 {}) m2)))


(comment
  (assoc nil 0 1)
  (assoc (fast-map {:a 1}) 0 4)
  (fast-map-merge2
    {:a 1}
    {:b 2}))


#_(comment
    (require '[criterium.core])

    (criterium.core/quick-bench
      (assoc {:a 1} :b 2))

    (criterium.core/quick-bench
      (fast-assoc {:a 1} :b 2))

    (let [m1 {}
          m1 {:a 1}
          m2 {}
          m2 {:b 2 :c 3 :d 4 :e 5}]
      #_(criterium.core/quick-bench
          (merge m1 m2))
      #_(criterium.core/quick-bench
          (merge2 m1 m2))
      #_(criterium.core/quick-bench
          (fast-map-merge m1 m2))
      (criterium.core/quick-bench
        (fast-map-merge m1 m2)))

    (let [m1 {}
          m1 {:a 1}
          m2 {:b 2 :c 3 :d 4 :e 5}
          m2 {:b 2}]
      (criterium.core/quick-bench
        (merge2 m1 m2))))

(defmacro inline-merge
  [& [m & ms]]
  (let [conjs# (map (fn [m] `(conj ~m)) ms)]
    `(-> (or ~m {})
       ~@conjs#)))


(defmacro inline-fast-map-merge
  [& [m & ms]]
  (let [conjs# (map (fn [m] `(fast-map-merge ~m)) ms)]
    `(-> (or ~m {})
       ~@conjs#)))

(defn- simple?
  [x]
  (or (keyword? x) (symbol? x) (string? x) (int? x)))

(defn- sequence?
  [xs]
  (or (vector? xs) (list? xs) (set? xs)))

(defn- try-resolve
  [sym]
  (when (symbol? sym)
    (when-let [r (resolve sym)]
      (deref r))))

(defn- simple-seq?
  [xs]
  (let [xs (or (try-resolve xs) xs)]
    (and (sequence? xs) (every? simple? xs))))

(defn- simple-seq
  [xs]
  (let [xs (or (try-resolve xs) xs)]
    (and (sequence? xs) (every? simple? xs) (seq xs))))


(comment
  (defmacro fast-get-in-th
    [m ks]
    {:pre [(vector? ks)]}
    `(-> ~m ~@ks)))

(defmacro inline-get-in
  "Like `get-in` but faster and uses code generation.
  `ks` must be either vector, list or set."
  [m ks]
  {:pre [(simple-seq? ks)]}
  (let [ks (simple-seq ks)
        chain#
        (map (fn [k] `(get ~k)) ks)]
    `(-> ~m ~@chain#)))

(defmacro inline-get-some-in
  [m ks]
  {:pre [(simple-seq? ks)]}
  (let [ks (simple-seq ks)
        sym (gensym "m__")
        steps
        (map (fn [step] `(if (nil? ~sym) nil (~sym ~step)))
          ks)]
    `(let [~sym ~m
           ~@(interleave (repeat sym) steps)]
       ~sym)))

(defn- destruct-map
  [m ks]
  (let [gmap (gensym "map__")
        syms (map (comp gensym symbol) ks)]
    (vec
      (concat `(~gmap ~m)
         (mapcat
          (fn [sym k]
            `(~sym (get ~gmap ~k)))
          syms
          ks)))))

(defn- extract-syms
  [bs]
  (map first (partition 2 (drop 2 bs))))

(defmacro inline-select-keys
  "Like `select-keys` but faster and uses code generation.
  `ks` must be either vector, list or set."
  [m ks]
  {:pre [(simple-seq? ks)]}
  (let [ks (simple-seq ks)
        bindings (destruct-map m ks)
        syms (extract-syms bindings)
        form (apply hash-map (interleave ks syms))]
    `(let ~bindings
       ~form)))

(comment
  (def ^:private cache (atom {}))

  (defn- anon-record
    [fields]
    (if-let [grec (get @cache fields)]
      grec
      (let [grec (gensym "Rec")]
        (println "defing record of name:" grec "with fields:" fields)
        (eval `(defrecord ~grec ~fields))
        (swap! cache assoc fields grec)
        grec)))

  (defmacro defrec->inline-select-keys
    "Like `select-keys` but faster and uses code generation.
  `ks` must be either vector, list or set."
    [m ks]
    {:pre [(simple-seq? ks)]}
    (let [ks (simple-seq ks)
          fields (mapv symbol ks)
          grec (anon-record fields)
          bindings (destruct-map m ks)
          syms (extract-syms bindings)]
      `(let ~bindings
         (~(symbol (str '-> grec)) ~@syms)))))


(defn- do-assoc-in
  [m ks v]
  (let [ks* (butlast ks)
        syms (repeatedly (inc (count ks*)) gensym)
        bs (loop [bs [(first syms) `(get ~m ~(first ks*))]
                  ks (next ks*)
                  syms (next syms)]
             (if ks
               (let [k (first ks)]
                 (recur (conj bs
                          (first syms)
                          `(get ~(last (butlast bs)) ~k))
                   (next ks)
                   (next syms)))
               bs))
        iter
        (fn iter
          [[sym & syms] [k & ks] v]
          (if ks
            `(assoc ~sym ~k ~(iter syms ks v))
            `(assoc ~sym ~k ~v)))]
    `(let ~bs
       ~(iter (list* m syms) ks v))))

(defmacro inline-assoc-in
  [m ks v]
  {:pre [(simple-seq? ks)]}
  (do-assoc-in m (simple-seq ks) v))

(defn do-update-in
  [m ks f & args]
  (let [ks* (butlast ks)
        syms (repeatedly (inc (count ks*)) gensym)
        bs (loop [bs [(first syms) `(get ~m ~(first ks*))]
                  ks (next ks*)
                  syms (next syms)]
             (if ks
               (let [k (first ks)]
                 (recur (conj bs
                          (first syms)
                          `(get ~(last (butlast bs)) ~k))
                   (next ks)
                   (next syms)))
               bs))
        iter
        (fn iter
          [[sym & syms] ks]
          (let [[k & ks] ks]
            (if ks
              `(assoc ~sym ~k ~(iter syms ks))
              `(assoc ~sym ~k (apply ~f (get ~sym ~k) ~@args)))))]
    `(let ~bs
       ~(iter (list* m syms) ks))))

(defmacro inline-update-in
  [m ks f & args]
  {:pre [(simple-seq? ks)]}
  (do-update-in m (simple-seq ks) f args))

