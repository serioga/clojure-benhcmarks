(ns clojure-benchmarks.fib
  (:require
    [criterium.core :as criterium])
  (:import
    (clojure.lang BigInt)))

(set! *warn-on-reflection* true)

(set! *unchecked-math* :warn-on-boxed)

#_(defn fib-tail
    [n second first]
    (cond
      (< n 3) (+ second first)
      true (recur (dec n) first (+ second first))))


(defn fibo [n]
  (loop [i n a 1N b 1N]
    (if (> i 0)
      (recur (dec i) b (+' a b))
      a)))

(defn fib-tail
  [^BigInt n, ^BigInt second, ^BigInt first]
  (cond
    (.lt n 3N)
    (.add second first)

    true
    (recur (.add n -1N), first, (.add second first))))

(defn fib
  [n]
  (fib-tail (bigint n) 0N 1N))


(defn fib2
  [^long n]
  (cond
    (= 0 n) 0
    (= 1 n) 1
    :else (loop [f-2 0N, f-1 1N, x 2]
            (let [f-0 (.add ^BigInt f-2 f-1)]
              (if (== x n)
                f-0
                (recur f-1 f-0 (inc x)))))))


(comment
  (let [a 1 b 2]
    (criterium/quick-bench
      (+ a b)))
  (let [a 1 b 2]
    (criterium/quick-bench
      (== a b)))
  (let [a 1 b 2]
    (criterium/quick-bench
      (unchecked-add a b)))
  (let [a (bigint 1) b (bigint 2)]
    (criterium/quick-bench
      (.add a b)))
  (let [a (bigint 1) b (bigint 2)]
    (criterium/quick-bench
      (unchecked-add a b)))
  (let [a (biginteger 1) b (biginteger 2)]
    (criterium/quick-bench
      (.add a b)))

  "0 1 1 2 3 5 8 13 21 34 55"
  (fib 1)
  (fib 2)
  (fib 3)
  (fib 10)
  (Long/MAX_VALUE)
  (time (fib 100000))
  (time (fib2 100000))
  (time (fibo 20)))


