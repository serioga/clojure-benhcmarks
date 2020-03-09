(ns clojure-benchmarks.unchecked-math
  (:require
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;; Allows both random and single-value initialization
(defn make-matrix-v [n & [val]]
  (long-array
    n (repeatedly
        n #(long-array
             n (repeatedly
                 n (fn [] (long (if val val (rand-int 10000)))))))))

(defn mult-matrix-v [a b]
  (let [n (count a)]
    (long-array
      n (for [i (range n)]
          (long-array
            n (for [j (range n)]
                (reduce (fn [sum k] (unchecked-add ^long sum
                                                   ^long (unchecked-multiply ^long (aget (aget a i) k)
                                                                             ^long (aget (aget b k) j))))
                        0 (range n))))))))


(comment
  (aget (long-array 10 (repeatedly 10 (constantly 1))) 3)
  (let [a (make-matrix-v 98)
        b (make-matrix-v 98)]
    (criterium/quick-bench (mult-matrix-v a b))))

