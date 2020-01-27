(ns clojure-benchmarks.chunked-seq
  (:require
    [clojure.string :as string]
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)


(def ^:dynamic *identity-count* (atom 0))
(def ^:dynamic *enable-identity-count* false)


(defn identity-count
  "Same as `identity` but counting calls if `*enable-identity-count*`."
  [x]
  (when *enable-identity-count*
    (swap! *identity-count* inc))
  x)


(defn run-with-identity-count*
  [f]
  (with-bindings {#'*enable-identity-count* true
                  #'*identity-count* (atom 0)}
    (let [x (f)]
      (println "identity-count:" @*identity-count*)
      x)))


(defmacro run-with-identity-count
  "Execute `expr` with *enable-identity-count* set to true.
   Print amount of calls to `identity-count` after that."
  [& expr]
  `(run-with-identity-count* (fn [] ~@expr)))


(defn test-first
  "Take first from lazy seq and return printed output."
  [xs]
  (->> xs
    (first)
    (run-with-identity-count)
    (with-out-str)
    (string/trim-newline)))


(comment

  (chunked-seq? '(1 2 3 45))
  #_false
  (chunked-seq? (seq '(1 2 3 45)))
  #_false
  (test-first '(1 2 3 45))
  #_"identity-count: 1"

  (chunked-seq? [1 2 3 4 5])
  #_false
  (chunked-seq? (seq [1 2 3 4 5]))
  #_true
  (test-first (map identity-count [1 2 3 4 5]))
  #_"identity-count: 5"

  (chunked-seq? {:a 1 :b 2})
  #_false
  (chunked-seq? (seq {:a 1 :b 2}))
  #_false
  (test-first (map identity-count {:a 1 :b 2}))
  #_"identity-count: 1"

  (chunked-seq? #{1 2 3 4 5})
  #_false
  (chunked-seq? (seq #{1 2 3 4 5}))
  #_false
  (test-first (map identity-count #{1 2 3 4 5}))
  #_"identity-count: 1"

  (chunked-seq? (range))
  #_false
  (chunked-seq? (seq (range)))
  #_false
  (test-first (map identity-count (range)))
  #_"identity-count: 1"

  (chunked-seq? (range 100))
  #_true
  (chunked-seq? (seq (range 100)))
  #_true
  (test-first (map identity-count (range 100)))
  #_"identity-count: 32"

  (chunked-seq? (eduction (map identity-count) (range)))
  #_false
  (chunked-seq? (seq (eduction (map identity-count) (range))))
  #_false
  (test-first (eduction (map identity-count) (range)))
  #_"identity-count: 33"

  (chunked-seq? (sequence (map identity-count) (range)))
  #_false
  (chunked-seq? (sequence (map identity-count) (range)))
  #_false
  (test-first (sequence (map identity-count) (range)))
  #_"identity-count: 32"

  (do
    "Reduce over chunked range"
    (criterium/quick-bench
      (reduce + (map inc (range 32)))))
  #_"Execution time mean : 1,363227 µs"
  (do
    "Reduce over list"
    (criterium/quick-bench
      (reduce + (map inc '(0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31)))))
  #_"Execution time mean : 4,787254 µs"
  (do
    "Reduce over vector (chunked)"
    (criterium/quick-bench
      (reduce + (map inc [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31]))))
  #_"Execution time mean : 1,591750 µs"
  (do
    "Transduce over finite range"
    (criterium/quick-bench
      (transduce (map inc) + (range 32))))
  #_"Execution time mean : 493,418400 ns"
  (do
    "Transduce over infinite range"
    (criterium/quick-bench
      (transduce (comp (take 32) (map inc)) + (range))))
  #_"Execution time mean : 1,522196 µs"
  (do
    "Transduce over list"
    (criterium/quick-bench
      (transduce (map inc) + '(0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31))))
  #_"Execution time mean : 629,370132 ns"
  (do
    "Transduce over vector"
    (criterium/quick-bench
      (transduce (map inc) + [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31])))
  #_"Execution time mean : 602,228853 ns"

  (run-with-identity-count
    (transduce (comp (map identity-count) (take 10)) + (range 100)))
  #_"identity-count: 10"

  :comment)