(ns clojure-benchmarks.destructuring
  (:require
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)


(comment ; No destructuring

  (do "Direct access to local binding"
      (let [a 1]
        (criterium/quick-bench
          a)))
  #_"Execution time mean : 5,789829 ns"

  (do "Let without destructuring"
      (let [a 1]
        (criterium/quick-bench
          (let [a a]
            a))))
  #_"Execution time mean : 5,776644 ns"

  :comment)


(comment ; Associative destructuring

  (do "Get from map, one name"
      (let [m {:a 1 :b 2 :c 3}]
        (criterium/quick-bench
          (let [a (m :a)]
            a))))
  #_"Execution time mean : 10,033420 ns"

  (do "Destructure from map, one name"
      (let [m {:a 1 :b 2 :c 3}]
        (criterium/quick-bench
          (let [{:keys [a]} m]
            a))))
  #_"Execution time mean : 42,811294 ns"

  (do "Destructure from map, two names"
      (let [m {:a 1 :b 2 :c 3}]
        (criterium/quick-bench
          (let [{:keys [a b]} m]
            a))))
  #_"Execution time mean : 50,447875 ns"

  (do "Destructure from map, three names"
      (let [m {:a 1 :b 2 :c 3}]
        (criterium/quick-bench
          (let [{:keys [a b c]} m]
            a))))
  #_"Execution time mean : 59,616641 ns"

  :comment)


(comment ; Sequential destructuring

  (do "Get from vector, first"
      (let [xs [1 2 3]]
        (criterium/quick-bench
          (let [a (xs 0)]
            a))))
  #_"Execution time mean : 5,987717 ns"

  (do "Destructure from vector, first"
      (let [xs [1 2 3]]
        (criterium/quick-bench
          (let [[a] xs]
            a))))
  #_"Execution time mean : 6,673867 ns"

  (do "Destructure from vector, first + second"
      (let [xs [1 2 3]]
        (criterium/quick-bench
          (let [[a b] xs]
            a))))
  #_"Execution time mean : 9,922274 ns"

  (do "Destructure from list, first"
      (let [xs '(1 2 3)]
        (criterium/quick-bench
          (let [[a] xs]
            a))))
  #_"Execution time mean : 117,358724 ns"

  (do "Destructure from vector, second"
      (let [xs [1 2 3]]
        (criterium/quick-bench
          (let [[_ b] xs]
            b))))
  #_"Execution time mean : 8,796105 ns"

  (do "Destructure from list, second"
      (let [xs '(1 2 3)]
        (criterium/quick-bench
          (let [[_ b] xs]
            b))))
  #_"Execution time mean : 233,437413 ns"

  (do "Destructure from vector, third"
      (let [xs [1 2 3]]
        (criterium/quick-bench
          (let [[_ _ c] xs]
            c))))
  #_"Execution time mean : 10,985130 ns"

  (do "Destructure from list, third"
      (let [xs '(1 2 3)]
        (criterium/quick-bench
          (let [[_ _ c] xs]
            c))))
  #_"Execution time mean : 355,095637 ns"

  :comment)
