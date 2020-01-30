(ns clojure-benchmarks.multi-reduce
  "Reducing into multiple values.
   Following https://hackernoon.com/faster-clojure-reduce-57a104448ea4."

  (:require
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)


(def sample (vec (range 1e6)))


(deftype PersistentAcc [sum-odd, sum-even])


(definterface IMutableAcc
  (add_odd [^long x])
  (add_even [^long x])
  (get_vec []))


(deftype MutableAcc [^:unsynchronized-mutable ^long sum-odd
                     ^:unsynchronized-mutable ^long sum-even]
  IMutableAcc
  (add_odd [this x] (set! sum-odd (+ sum-odd x)), this)
  (add_even [this x] (set! sum-even (+ sum-even x)), this)
  (get_vec [_] [sum-odd, sum-even]))


(comment
  (do
    "Reduce single value"
    (criterium/quick-bench
      (reduce (fn [^long acc, ^long x]
                (if (odd? x) (+ acc x), acc))
        0 sample)
      #_250000000000))
  #_"Execution time mean : 15,297388 ms"

  (do
    "Loop single value"
    (criterium/quick-bench
      (loop [xs (seq sample)
             acc (long 0)]
        (if-some [x (first xs)]
          (if (odd? x)
            (recur (next xs) (+ acc ^long x))
            (recur (next xs) acc))
          acc))
      #_250000000000))
  #_"Execution time mean : 20,607538 ms"

  (do
    "Reduce persistent map with update, hidden boxed math"
    (criterium/quick-bench
      (reduce (fn [acc, ^long x]
                (if (odd? x)
                  (update acc :sum-odd + x)
                  (update acc :sum-even + x)))
        {:sum-odd 0 :sum-even 0} sample)
      #_{:sum-odd 250000000000, :sum-even 249999500000}))
  #_"Execution time mean : 135,633931 ms"

  (do
    "Reduce persistent map with update, no boxed math"
    (criterium/quick-bench
      (reduce (fn [acc, ^long x]
                (if (odd? x)
                  (update acc :sum-odd (fn [^long acc] (+ acc x)))
                  (update acc :sum-even (fn [^long acc] (+ acc x)))))
        {:sum-odd 0 :sum-even 0} sample)
      #_{:sum-odd 250000000000, :sum-even 249999500000}))
  #_"Execution time mean : 126,737731 ms"

  (do
    "Reduce persistent map with assoc"
    (criterium/quick-bench
      (reduce (fn [acc, ^long x]
                (if (odd? x)
                  (assoc acc :sum-odd (+ x ^long (acc :sum-odd)))
                  (assoc acc :sum-even (+ x ^long (acc :sum-even)))))
        {:sum-odd 0 :sum-even 0} sample)
      #_{:sum-odd 250000000000, :sum-even 249999500000}))
  #_"Execution time mean : 61,444073 ms"
  #_"Result: `get`+`assoc` is faster than `update`"

  (do
    "Reduce transient map"
    (criterium/quick-bench
      (persistent!
        (reduce (fn [acc, ^long x]
                  (if (odd? x)
                    (assoc! acc :sum-odd (+ x ^long (acc :sum-odd)))
                    (assoc! acc :sum-even (+ x ^long (acc :sum-even)))))
          (transient {:sum-odd 0 :sum-even 0}) sample))
      #_{:sum-odd 250000000000, :sum-even 249999500000}))
  #_"Execution time mean : 29,871736 ms"
  #_"Result: transient is 2× faster than persistent, looks like optimal solution in general"

  (do
    "Reduce volatiles with reset"
    (criterium/quick-bench
      (let [sum-odd (volatile! 0)
            sum-even (volatile! 0)]
        (reduce (fn [_, ^long x]
                  (if (odd? x)
                    (vreset! sum-odd (+ x ^long @sum-odd))
                    (vreset! sum-even (+ x ^long @sum-even))))
          nil sample)
        [@sum-odd, @sum-even])
      #_[250000000000 249999500000]))
  #_"Execution time mean : 30,101215 ms"
  #_"Result: Same as transient map but less idiomatic"

  (do
    "Reduce volatiles with swap"
    (criterium/quick-bench
      (let [sum-odd (volatile! 0)
            sum-even (volatile! 0)]
        (reduce (fn [_, ^long x]
                  (if (odd? x)
                    (vswap! sum-odd (fn [^long acc] (+ acc x)))
                    (vswap! sum-even (fn [^long acc] (+ acc x)))))
          nil sample)
        [@sum-odd, @sum-even])
      #_[250000000000 249999500000]))
  #_"Execution time mean : 20,739801 ms"
  #_"Result: Volatiles with `vswap!` are faster than `vreset!`"

  (do
    "Reduce native array"
    (criterium/quick-bench
      (vec
        (reduce (fn [^longs arr, ^long x]
                  (if (odd? x)
                    (aset arr 0 (+ x (aget arr 0)))
                    (aset arr 1 (+ x (aget arr 1))))
                  arr)
          (long-array [0 0]) sample))
      #_[250000000000 249999500000]))
  #_"Execution time mean : 14,625961 ms"
  #_"Result: Fastest but too cryptic (positional identifying of accumulated values, works for values of same types only)"


  (do
    "Reduce persistent custom type"
    (criterium/quick-bench
      (let [acc ^PersistentAcc (reduce (fn [^PersistentAcc acc, ^long x]
                                         (if (odd? x)
                                           (PersistentAcc. (+ x ^long (.-sum_odd acc)), (.-sum_even acc))
                                           (PersistentAcc. (.-sum_odd acc), (+ x ^long (.-sum_even acc)))))
                                 (PersistentAcc. 0 0) sample)]
        [(.-sum_odd acc), (.-sum_even acc)])
      #_[250000000000 249999500000]))
  #_"Execution time mean : 20,781628 ms"
  #_"Result: As fast as volatiles but without mutation"


  (do
    "Reduce mutable custom type"
    (criterium/quick-bench
      (.get_vec ^MutableAcc (reduce (fn [^MutableAcc acc, ^long x]
                                      (if (odd? x)
                                        (.add_odd acc x)
                                        (.add_even acc x)))
                              (MutableAcc. 0 0) sample))
      #_[250000000000 249999500000]))
  #_"Execution time mean : 13,564725 ms"
  #_"Result: Faster than native array, also more flexible and understandable"


  #_'comment)
