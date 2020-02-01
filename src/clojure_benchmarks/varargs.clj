(ns clojure-benchmarks.varargs
  "Multi-arity vs varargs")

(set! *warn-on-reflection* true)


(defn test-arity
  "Multi arity"
  ([x]
   [x nil nil])

  ([x y]
   [x y nil])

  ([x y z]
   [x y z]))


(defn test-var-1
  "Varargs from second arg"
  [x & [y z]]
  [x y z])


(defn test-var-0
  "Varargs from first arg"
  [& [x y z]]
  [x y z])


(comment

  ; Multi arity

  (criterium.core/quick-bench
    (test-arity 1))
  #_"Execution time mean : 10,972841 ns"

  (criterium.core/quick-bench
    (test-arity 1 2))
  #_"Execution time mean : 11,775521 ns"

  (criterium.core/quick-bench
    (test-arity 1 2 3))
  #_"Execution time mean : 11,830639 ns"


  ; Varargs from second arg

  (criterium.core/quick-bench
    (test-var-1 1))
  #_"Execution time mean : 10,857151 ns"

  (criterium.core/quick-bench
    (test-var-1 1 2))
  #_"Execution time mean : 235,658353 ns"

  (criterium.core/quick-bench
    (test-var-1 1 2 3))
  #_"Execution time mean : 243,787998 ns"


  ; Varargs from first arg

  (criterium.core/quick-bench
    (test-var-0 1))
  #_"Execution time mean : 340,140384 ns"

  (criterium.core/quick-bench
    (test-var-0 1 2))
  #_"Execution time mean : 353,926959 ns"

  (criterium.core/quick-bench
    (test-var-0 1 2 3))
  #_"Execution time mean : 363,534180 ns"

  #_'comment)
