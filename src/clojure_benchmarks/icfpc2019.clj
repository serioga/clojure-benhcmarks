(ns clojure-benchmarks.icfpc2019
  (:require [criterium.core :as criterium]))

(set! *warn-on-reflection* true)


(comment
  (let [dx 0 dy 0]
    (criterium/quick-bench
      (= [0 0] [dx dy])))

  (let [dx 0 dy 0 z [0 0]]
    (criterium/quick-bench
      (= z [dx dy])))

  (let [dx 0 dy 1]
    (criterium/quick-bench
      (= [0 0] [dx dy])))
  (let [dx 1 dy 1]
    (criterium/quick-bench
      (= [0 0] [dx dy])))
  (let [dx 0 dy 0]
    (criterium/quick-bench
      (and (zero? dx) (zero? dy))))
  (let [dx 1 dy 1]
    (criterium/quick-bench
      (and (zero? dx) (zero? dy))))
  (let [m {:a 1 :b 2}]
    (criterium/quick-bench
      (:b m)))
  (let [m {:a 1 :b 2}]
    (criterium/quick-bench
      (get m :b)))
  (let [m {:a 1 :b 2}]
    (criterium/quick-bench
      (m :b)))
  (let [m {:a 1 :b 2}]
    (criterium/quick-bench
      (.valAt m :b)))
  (criterium/quick-bench
    (< 1 2 3))
  (criterium/quick-bench
    (and
      (< 1 2)
      (< 2 3))))
