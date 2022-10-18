(ns clojure-benchmarks.lazy-map
  (:require [lazy-map.core :as m])
  (:import (clojure.lang Delay IFn IKeywordLookup ILookup ILookupThunk)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def -m1 {:x :value})

(def -m2 (m/lazy-map {:x :value}))

(def -m3 (let [^Delay -k (delay :value)]
           (reify
             ILookup
             (valAt [_ k]
               (case k
                 :x (.deref -k)
                 nil))
             (valAt [_ k not-found]
               (case k
                 :x (.deref -k)
                 not-found))
             IFn
             (invoke [_ k]
               (case k
                 :x (.deref -k)
                 nil))
             IKeywordLookup
             (getLookupThunk [_ k]
               (case k
                 :x (reify ILookupThunk (get [_ _] (.deref -k)))
                 nil)))))

(comment
  ;; Core map

  (get -m1 :x)
  ;             Execution time mean : 5,747938 ns
  ;    Execution time std-deviation : 0,496747 ns
  ;   Execution time lower quantile : 5,227652 ns ( 2,5%)
  ;   Execution time upper quantile : 6,337587 ns (97,5%)

  (-m1 :x)
  ;             Execution time mean : 4,750021 ns
  ;    Execution time std-deviation : 0,196149 ns
  ;   Execution time lower quantile : 4,543379 ns ( 2,5%)
  ;   Execution time upper quantile : 5,036991 ns (97,5%)

  (:x -m1)
  ;             Execution time mean : 6,856878 ns
  ;    Execution time std-deviation : 0,376985 ns
  ;   Execution time lower quantile : 6,378641 ns ( 2,5%)
  ;   Execution time upper quantile : 7,286718 ns (97,5%)

  (get -m1 :y)
  ;             Execution time mean : -0,787393 ns
  ;    Execution time std-deviation : 0,136389 ns
  ;   Execution time lower quantile : -0,930790 ns ( 2,5%)
  ;   Execution time upper quantile : -0,566485 ns (97,5%)

  (get -m1 :y :z)
  ;             Execution time mean : -0,706818 ns
  ;    Execution time std-deviation : 0,294116 ns
  ;   Execution time lower quantile : -1,040608 ns ( 2,5%)
  ;   Execution time upper quantile : -0,307510 ns (97,5%)

  ;; Lazy-map

  (get -m2 :x)
  ;             Execution time mean : 12,166154 ns
  ;    Execution time std-deviation : 0,247685 ns
  ;   Execution time lower quantile : 11,890449 ns ( 2,5%)
  ;   Execution time upper quantile : 12,492178 ns (97,5%)

  (-m2 :x)
  ;Execution error (ClassCastException)

  (:x -m2)
  ;             Execution time mean : 13,320414 ns
  ;    Execution time std-deviation : 0,308002 ns
  ;   Execution time lower quantile : 12,888707 ns ( 2,5%)
  ;   Execution time upper quantile : 13,568496 ns (97,5%)

  (get -m2 :y)
  ;             Execution time mean : 5,452250 ns
  ;    Execution time std-deviation : 0,997035 ns
  ;   Execution time lower quantile : 4,333263 ns ( 2,5%)
  ;   Execution time upper quantile : 6,592631 ns (97,5%)

  (get -m2 :y :z)
  ;             Execution time mean : 7,273651 ns
  ;    Execution time std-deviation : 0,334796 ns
  ;   Execution time lower quantile : 6,896928 ns ( 2,5%)
  ;   Execution time upper quantile : 7,609194 ns (97,5%)

  ;; Hand-made lazy-map

  (get -m3 :x)
  ;             Execution time mean : 10,560831 ns
  ;    Execution time std-deviation : 0,346979 ns
  ;   Execution time lower quantile : 10,241487 ns ( 2,5%)
  ;   Execution time upper quantile : 11,111281 ns (97,5%)

  (-m3 :x)
  ;             Execution time mean : 4,421001 ns
  ;    Execution time std-deviation : 0,309477 ns
  ;   Execution time lower quantile : 4,045385 ns ( 2,5%)
  ;   Execution time upper quantile : 4,833785 ns (97,5%)

  (:x -m3)
  ;             Execution time mean : 2,874699 ns
  ;    Execution time std-deviation : 0,150604 ns
  ;   Execution time lower quantile : 2,697523 ns ( 2,5%)
  ;   Execution time upper quantile : 3,014733 ns (97,5%)

  (get -m3 :y)
  ;             Execution time mean : 2,499061 ns
  ;    Execution time std-deviation : 0,127236 ns
  ;   Execution time lower quantile : 2,358203 ns ( 2,5%)
  ;   Execution time upper quantile : 2,674450 ns (97,5%)

  (get -m3 :y :z)
  ;             Execution time mean : 3,160227 ns
  ;    Execution time std-deviation : 0,143133 ns
  ;   Execution time lower quantile : 2,988231 ns ( 2,5%)
  ;   Execution time upper quantile : 3,344003 ns (97,5%)

  (:y -m3)
  ;             Execution time mean : 3,513049 ns
  ;    Execution time std-deviation : 0,155689 ns
  ;   Execution time lower quantile : 3,303812 ns ( 2,5%)
  ;   Execution time upper quantile : 3,675837 ns (97,5%)

  (:y -m3 :z)
  ;             Execution time mean : -2,069384 ns
  ;    Execution time std-deviation : 0,100685 ns
  ;   Execution time lower quantile : -2,171200 ns ( 2,5%)
  ;   Execution time upper quantile : -1,967762 ns (97,5%)

  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
