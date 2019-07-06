(ns clojure-benchmarks.fib
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
  [^BigInteger n ^BigInteger second ^BigInteger first]
  (cond
    (= -1 (.compareTo n (biginteger 3)))

    (.add second first)

    true

    (recur
      (.subtract n (biginteger 1))
      first
      (.add second first))))

(defn fib
  [n]
  (fib-tail (biginteger n) (biginteger 0) (biginteger 1)))

(comment
  "0 1 1 2 3 5 8 13 21 34 55"
  (fib 1)
  (fib 2)
  (fib 3)
  (fib 10)
  (Long/MAX_VALUE)
  (time (fib 100000))
  (time (fibo 100000)))
