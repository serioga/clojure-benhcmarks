(ns clojure-benchmarks.transducers
  (:require
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)


(defn test-transform
  [^long x]
  (when (odd? x) (inc x)))


(comment
  (->> (range 10)
    (keep test-transform)
    (into []))
  #_[2 4 6 8 10]

  (do
    "Range (chunked), filterv/mapv"
    (criterium/quick-bench
      (->> (range 10)
        (filterv odd?)
        (mapv inc))))
  #_"Execution time mean : 442,601305 ns"

  (do
    "Range (chunked), traditional, into"
    (criterium/quick-bench
      (->> (range 10)
        (keep test-transform)
        (into []))))
  #_"Execution time mean : 870,861921 ns"

  (do
    "Range (chunked), traditional, vec"
    (criterium/quick-bench
      (->> (range 10)
        (keep test-transform)
        (vec))))
  #_"Execution time mean : 620,709544 ns"

  (do
    "Range (chunked), transduce, into"
    (criterium/quick-bench
      (->> (range 10)
        (into [] (keep test-transform)))))
  #_"Execution time mean : 266,767011 ns"

  (do
    "Range (chunked), transduce, conj"
    (criterium/quick-bench
      (->> (range 10)
        (transduce (keep test-transform) conj))))
  #_"Execution time mean : 308,451779 ns"

  (do
    "Vector, filterv/mapv"
    (criterium/quick-bench
      (->> [0 1 2 3 4 5 6 7 8 9]
        (filterv odd?)
        (mapv inc))))
  #_"Execution time mean : 457,845749 ns"

  (do
    "Vector, traditional, into"
    (criterium/quick-bench
      (->> [0 1 2 3 4 5 6 7 8 9]
        (keep test-transform)
        (into []))))
  #_"Execution time mean : 924,425251 ns"

  (do
    "Vector, traditional, vec"
    (criterium/quick-bench
      (->> [0 1 2 3 4 5 6 7 8 9]
        (keep test-transform)
        (vec))))
  #_"Execution time mean : 576,970687 ns"

  (do
    "Vector, transduce, into"
    (criterium/quick-bench
      (->> [0 1 2 3 4 5 6 7 8 9]
        (into [] (keep test-transform)))))
  #_"Execution time mean : 353,753392 ns"

  (do
    "Vector, transduce, conj"
    (criterium/quick-bench
      (->> [0 1 2 3 4 5 6 7 8 9]
        (transduce (keep test-transform) conj))))
  #_"Execution time mean : 368,914235 ns"

  (do
    "List, filterv/mapv"
    (criterium/quick-bench
      (->> '(0 1 2 3 4 5 6 7 8 9)
        (filterv odd?)
        (mapv inc))))
  #_"Execution time mean : 407,804544 ns"

  (do
    "List, traditional, into"
    (criterium/quick-bench
      (->> '(0 1 2 3 4 5 6 7 8 9)
        (keep test-transform)
        (into []))))
  #_"Execution time mean : 1,581709 µs"

  (do
    "List, traditional, vec"
    (criterium/quick-bench
      (->> '(0 1 2 3 4 5 6 7 8 9)
        (keep test-transform)
        (vec))))
  #_"Execution time mean : 1,067706 µs"

  (do
    "List, transduce, into"
    (criterium/quick-bench
      (->> '(0 1 2 3 4 5 6 7 8 9)
        (into [] (keep test-transform)))))
  #_"Execution time mean : 267,802853 ns"

  (do
    "List, transduce, conj"
    (criterium/quick-bench
      (->> '(0 1 2 3 4 5 6 7 8 9)
        (transduce (keep test-transform) conj))))
  #_"Execution time mean : 278,435039 ns"


  :comment)

