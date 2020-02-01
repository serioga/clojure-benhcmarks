(ns clojure-benchmarks.select-keys
  (:require
    [clojure-benchmarks.clj-fast :as clj-fast]
    [criterium.core :as criterium])
  (:import
    (clojure.lang Associative)))

(set! *warn-on-reflection* true)


(def sample (-> {:a 1, :b 2, :c nil, :d 4, :e 5}
              (with-meta {:meta 'sample})))


(defn fast-assoc
  "Assoc using direct call to `Associative.assoc`."
  [m k v]
  (let [m (or m {})]
    (.assoc ^Associative m k v)))


(comment
  (criterium/quick-bench
    (empty sample))
  #_"Execution time mean : 7,610230 ns"

  (criterium/quick-bench
    (meta (empty sample)))
  #_"Execution time mean : 102,761457 ns"

  (criterium/quick-bench
    (select-keys sample [:a :b :c :x :y])
    #_{:a 1, :b 2, :c nil})
  #_"Execution time mean : 593,912509 ns"

  (criterium.core/quick-bench
    (into {} (keep (fn [k] (let [v (sample k ::not-found)]
                             (when-not (identical? v ::not-found)
                               [k v]))))
      [:a :b :c :x :y])
    #_{:a 1, :b 2, :c nil})
  #_"Execution time mean : 421,003387 ns"

  (criterium/quick-bench
    (reduce (fn [acc k] (let [v (sample k ::not-found)]
                          (if (identical? v ::not-found)
                            acc
                            (assoc acc k v))))
      {} [:a :b :c :x :y])
    #_{:a 1, :b 2, :c nil})
  #_"Execution time mean : 112,734195 ns"

  (criterium/quick-bench
    (reduce (fn [acc k] (let [v (sample k ::not-found)]
                          (if (identical? v ::not-found)
                            acc
                            (fast-assoc acc k v))))
      {} [:a :b :c :x :y])
    #_{:a 1, :b 2, :c nil})
  #_"Execution time mean : 106,780543 ns"

  (criterium/quick-bench
    (persistent!
      (reduce (fn [acc k] (let [v (sample k ::not-found)]
                            (if (identical? v ::not-found)
                              acc
                              (assoc! acc k v))))
        (transient {}) [:a :b :c :x :y]))
    #_{:a 1, :b 2, :c nil})
  #_"Execution time mean : 132,476165 ns"


  #_'comment)


(comment #_"Invalid implementations"

  (criterium.core/quick-bench
    (into {} (keep (fn [k] (when-some [v (sample k)]
                             [k v])))
      [:a :b :c :x :y])
    #_{:a 1, :b 2})
  #_"Execution time mean : 372,461332 ns"

  (criterium/quick-bench
    (reduce (fn [acc k] (if-some [v (sample k)]
                          (assoc acc k v)
                          acc))
      {} [:a :b :c :x :y])
    #_{:a 1, :b 2})
  #_"Execution time mean : 82,280657 ns"

  (criterium/quick-bench
    (reduce (fn [acc k] (if-some [v (sample k)]
                          (fast-assoc acc k v)
                          acc))
      {} [:a :b :c :x :y])
    #_{:a 1, :b 2})
  #_"Execution time mean : 81,708246 ns"

  (criterium/quick-bench
    (persistent!
      (reduce (fn [acc k] (if-some [v (sample k)]
                            (assoc! acc k v)
                            acc))
        (transient {}) [:a :b :c :x :y]))
    #_{:a 1, :b 2})
  #_"Execution time mean : 113,563581 ns"

  (criterium/quick-bench
    (clj-fast/inline-select-keys sample [:a :b :c :x :y])
    #_{:y nil, :c 3, :b 2, :x nil, :a 1})
  #_"Execution time mean : 30,522068 ns"

  (macroexpand-1 '(clj-fast/inline-select-keys sample [:a :b :c :x :y]))
  #_(clojure.core/let
      [map__3903 sample
       a3904 (clojure.core/get map__3903 :a)
       b3905 (clojure.core/get map__3903 :b)
       c3906 (clojure.core/get map__3903 :c)
       x3907 (clojure.core/get map__3903 :x)
       y3908 (clojure.core/get map__3903 :y)]
      {:y y3908, :c c3906, :b b3905, :x x3907, :a a3904})

  'comment #_"Invalid implementations")
