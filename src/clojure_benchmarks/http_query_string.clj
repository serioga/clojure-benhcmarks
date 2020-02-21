(ns clojure-benchmarks.http-query-string
  (:require
    [clojure.string :as string]
    [clojure.test :as t]
    [criterium.core]
    [net.cgrand.xforms.rfs :as rfs]
    [reitit.impl])
  (:import
    (clojure.lang MapEntry)
    (java.net URLDecoder URLEncoder)))

(set! *warn-on-reflection* true)

(defn parse-query-params
  "Convert query string to sequence of `k=v` strings."
  {:test (fn []
           (t/is (= ["x=1" "y=2"] (parse-query-params "x=1&y=2"))))}
  [s]
  (when s
    (string/split s #"&")))

#_(defn parse-query-params
    "Convert query string to sequence of `k=v` strings."
    [s]
    (when s
      (re-seq #"[^&]+" s)))

(defn kv-pair [k v] (MapEntry/create k v))
(defn kv-key [kv] (kv 0))
(defn kv-val [kv] (kv 1))

(defn ^:private query-param->kv-pair
  "Parse query param `k=v` in kv-pair representation."
  {:test (fn []
           (t/is (= ["" ""] (query-param->kv-pair "")))
           (t/is (= ["" ""] (query-param->kv-pair "=")))
           (t/is (= ["x" ""] (query-param->kv-pair "x")))
           (t/is (= ["x" ""] (query-param->kv-pair "x=")))
           (t/is (= ["" "1"] (query-param->kv-pair "=1")))
           (t/is (= ["x" "1"] (query-param->kv-pair "x=1")))
           (t/is (= ["x x" "1 1"] (query-param->kv-pair "x+x=1+1")))
           (t/is (= ["x x" "1 1"] (query-param->kv-pair "x%20x=1%201"))))}
  ([s] (query-param->kv-pair s, identity, identity))
  ([s, key-fn, val-fn]
   (let [kv (string/split s #"=" 2)]
     (kv-pair
       (key-fn (URLDecoder/decode (kv 0) "UTF-8"))
       (val-fn (URLDecoder/decode (get kv 1 "") "UTF-8"))))))

(defn ^:private xf:query-params->kv-pairs
  "Transformation of query param `k=v` in kv-pair representation."
  [key-fn, val-fn]
  (map #(query-param->kv-pair % key-fn val-fn)))

(defn query-string->map-simplified
  "Collect query params to map, duplicate keys are overwritten."
  {:test (fn []
           (t/is (= {"x" "2", "y" "2"} (query-string->map-simplified "x=1&x=2&y=2"))))}
  ([s] (query-string->map-simplified s, identity, identity))
  ([s, key-fn, val-fn]
   (transduce (xf:query-params->kv-pairs key-fn val-fn)
     conj {} (parse-query-params s))))

(defn ^:private rf:kv-pairs->map
  "Reducing function kv-pairs to hash-map.
   Values of duplicate keys are collected as vector of values."
  ([] {})
  ([m] m)
  ([m kv] (let [k (kv-key kv)
                v (m k)
                v (cond
                    (nil? v), (kv-val kv)
                    (vector? v), (conj v (kv-val kv))
                    :else [v (kv-val kv)])]
            (assoc m k v))))

(defn query-string-map
  "Collect query params to map, duplicate keys are collected as vector of values."
  {:test (fn []
           (t/is (= {"x" ["1" "2"], "y" "2"} (query-string-map "x=1&x=2&y=2"))))}
  ([s] (query-string-map s, identity, identity))
  ([s, key-fn, val-fn]
   (transduce (xf:query-params->kv-pairs key-fn, val-fn)
     rf:kv-pairs->map (parse-query-params s))))

(defn query-string-seq
  "Collect query params to sequence of kv-pairs."
  {:test (fn []
           (t/is (= '(["x" "1"] ["x" "2"] ["y" "2"]) (query-string-seq "x=1&x=2&y=2"))))}
  ([s] (query-string-seq s, identity, identity))
  ([s, key-fn, val-fn]
   (sequence (xf:query-params->kv-pairs key-fn, val-fn) (parse-query-params s))))

(defn ^:private data-seq?
  "If data item should be processed sequentially."
  [x]
  (or (coll? x) (sequential? x)))

(defn ^:private xf:data->kv-pair
  "Transform data sequence item to query string kv-pair."
  [rf]
  (let [rf-vals (fn [result k vs]
                  (transduce (map (partial kv-pair k)) (completing rf identity) result vs))]
    (fn data->kv-pair
      ([] (rf))
      ([result] (rf result))
      ([result x] (cond
                    (vector? x) (case (count x)
                                  0 (rf result (kv-pair "" ""))
                                  1 (rf result (kv-pair (x 0) ""))
                                  2 (let [k (x 0), v (x 1)]
                                      (cond
                                        (data-seq? v), (rf-vals result k v)
                                        :else (rf result (kv-pair k v))))
                                  (rf-vals result (x 0) (subvec x 1)))
                    :else (rf result (kv-pair x "")))))))

(defn ^:private xf:kv-pair->query-string
  "Transforms kv-pair to query string tokens.
   (!) Produces extra `&` before first item."
  ([] (xf:kv-pair->query-string name, str))
  ([key-fn, val-fn]
   (fn
     [rf]
     (fn kv-pair->query-string
       ([] (rf))
       ([result] (rf result))
       ([result kv] (-> result
                      (rf "&")
                      (rf (URLEncoder/encode (key-fn (kv-key kv)) "UTF-8"))
                      (rf "=")
                      (rf (URLEncoder/encode (val-fn (kv-val kv)) "UTF-8"))))))))

(def ^:private rf:str-drop-first
  (completing rfs/str (fn [^StringBuilder sb]
                        (rfs/str (cond-> sb
                                   (pos? (.length sb)) (.deleteCharAt 0))))))

(defn ->query-string
  "Convert data sequence to query string."
  {:test (fn []
           (t/is (= "a=1&b=2&c=3&d=4"
                   (->query-string [["a" "1"] ["b" "2"] ["c" "3"] ["d" "4"]])))
           (t/is (= "a=&b=&c=&d="
                   (->query-string [["a" ""] ["b" ""] ["c" ""] ["d" ""]])))
           (t/is (= "a=&b=&c=&d="
                   (->query-string ["a" "b" "c" "d"])))
           (t/is (= "a=&b=&c=&d="
                   (->query-string [["a"] ["b"] ["c"] ["d"]])))
           (t/is (= "a=1&a=2&b=1&b=2&c=1&c=2&d=1&d=2"
                   (->query-string [["a" "1" "2"] ["b" "1" "2"] ["c" "1" "2"] ["d" "1" "2"]])))
           (t/is (= "a=1&a=2&b=1&b=2&c=1&c=2&d=1&d=2"
                   (->query-string [["a" ["1" "2"]] ["b" ["1" "2"]] ["c" ["1" "2"]] ["d" ["1" "2"]]])))
           (t/is (= "a=0&a=1&a=2&a=3&a=4"
                   (->query-string [["a" (range 5)]])))
           (t/is (= ""
                   (->query-string [["a" []]])))
           (t/is (nil?
                   (->query-string "abc")))
           (t/is (nil?
                   (->query-string nil))))}
  ([data] (->query-string name str data))
  ([key-fn, val-fn, data]
   (when (data-seq? data)
     (transduce (comp
                  xf:data->kv-pair
                  (xf:kv-pair->query-string key-fn val-fn))
       rf:str-drop-first, data))))

(t/deftest test:hither-and-thither
  (t/is (= "x=1&y=2&z=3&x=4+5" (-> "x=1&y=2&z=3&x=4+5"
                                 (query-string-seq)
                                 (->query-string)))))

(comment

  (criterium.core/quick-bench
    (->query-string [])
    #_"")
  #_"Execution time mean : 60,162402 ns"

  (criterium.core/quick-bench
    (reitit.impl/query-string [])
    #_"")
  #_"Execution time mean : 81,846670 ns"

  (criterium.core/quick-bench
    (->query-string "")
    #_nil)
  #_"Execution time mean : 44,305201 ns"

  (criterium.core/quick-bench
    (reitit.impl/query-string "")
    #_"")
  #_"Execution time mean : 121,156767 ns"

  (criterium.core/quick-bench
    (->query-string nil)
    #_nil)
  #_"Execution time mean : 7,903445 ns"

  (criterium.core/quick-bench
    (reitit.impl/query-string nil)
    #_"")
  #_"Execution time mean : 73,842325 ns"

  (criterium.core/quick-bench
    (reitit.impl/query-string {:a ["1" "2" "3" "4" "5"]})
    #_"a=1&a=2&a=3&a=4&a=5")
  #_"Execution time mean : 2,959894 µs"

  (criterium.core/quick-bench
    (->query-string {:a ["1" "2" "3" "4" "5"]})
    #_"a=1&a=2&a=3&a=4&a=5")
  #_"Execution time mean : 1,032663 µs"

  (criterium.core/quick-bench
    (->query-string [[:a "1"] [:a "2"] [:a "3"] [:a "4"] [:a "5"]])
    #_"a=1&a=2&a=3&a=4&a=5")
  #_"Execution time mean : 1,637415 µs"

  (criterium.core/quick-bench
    (->query-string {:a "1" :b "1" :c "1" :d "1" :e "1"})
    #_"a=1&b=1&c=1&d=1&e=1")
  #_"Execution time mean : 1,808396 µs"

  (criterium.core/quick-bench
    (reitit.impl/query-string {:a "1" :b "1" :c "1" :d "1" :e "1"})
    #_"a=1&b=1&c=1&d=1&e=1")
  #_"Execution time mean : 2,649090 µs"

  (criterium.core/quick-bench
    (->query-string [:a "b" ["c"] ["d" "1"] ["e" ["1"]] ["f" "1" "2"]])
    #_"a=&b=&c=&d=1&e=1&f=1&f=2")
  #_"Execution time mean : 1,756097 µs"

  (criterium.core/quick-bench
    (into [] xf:data->kv-pair [:a "b" ["c"] ["d" "1"] ["e" ["1"]] ["f" "1" "2"]])
    #_[[:a ""] ["b" ""] ["c" ""] ["d" "1"] ["e" "1"] ["f" "1"] ["f" "2"]])
  #_"Execution time mean : 1,019710 µs"

  (->query-string [["a" ["1" "2"] [1 2]] ["b" ["1" "2"]] ["c" ["1" "2"]] ["d" ["1" "2"]]])
  #_"a=%5B%221%22+%222%22%5D&a=%5B1+2%5D&b=1&b=2&c=1&c=2&d=1&d=2"

  (criterium.core/quick-bench
    (-> "x=1&y=2&z=3"
      (query-string-seq)
      (->query-string)))
  #_"Execution time mean : 2,670347 µs"

  (criterium.core/quick-bench
    (-> "x=1&y=2&z=3"
      (query-string-seq)
      (reitit.impl/query-string)))
  #_"Execution time mean : 3,004262 µs"

  (criterium.core/quick-bench
    (query-string-map "x=1&y=2&z=3")
    #_{"x" "1", "y" "2", "z" "3"})
  #_"Execution time mean : 1,477243 µs"

  (criterium.core/quick-bench
    (query-string->map-simplified "x=1&y=2&z=3")
    #_{"x" "1", "y" "2", "z" "3"})
  #_"Execution time mean : 1,387769 µs"


  'comment)
