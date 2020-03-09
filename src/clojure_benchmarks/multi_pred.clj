(ns clojure-benchmarks.multi-pred
  "Find sequence element by multiple ordered predicates.

   https://t.me/clojure_ru/109469
   https://t.me/clojure_ru/109485
   https://t.me/clojure_ru/109505"
  (:import
    (clojure.lang MapEntry)))

(set! *warn-on-reflection* true)


(def dataset1-opt
  "Optimistic dataset"
  [{:text "789" :type "physical"}
   {:text "123" :type "postal"}
   {:text "---" :type "test"}
   {:text "---" :type "test"}
   {:text "---" :type "test"}])

(def dataset1-pes
  "Pessimistic dataset"
  [{:text "---" :type "test"}
   {:text "---" :type "test"}
   {:text "---" :type "test"}
   {:text "123" :type "postal"}
   {:text "789" :type "physical"}])

(def dataset2
  "Dataset to match secondary predicate."
  [{:text "789" :type "other"}
   {:text "123" :type "postal"}
   {:text "---" :type "test"}
   {:text "---" :type "test"}
   {:text "---" :type "test"}])

(def dataset3
  "Dataset to match nothing"
  [{:text "789" :type "other"}
   {:text "123" :type "yet-another"}
   {:text "---" :type "test"}
   {:text "---" :type "test"}
   {:text "---" :type "test"}])


(defn find-group-by
  "https://t.me/clojure_ru/109485"
  [xs]
  (let [m (group-by :type xs)]
    (or (m "physical")
        (m "postal"))))


(defn find-loop
  "https://t.me/clojure_ru/109505"
  [xs]
  (loop [physical nil, postal nil
         [{:keys [type text]} :as r] xs]
    (cond
      physical physical
      (empty? r) postal
      :else (recur ({"physical" text} type)
                   (or postal
                       ({"postal" text} type))
                   (rest r)))))


(defn find-priority-pred
  "Take sequence of predicated and find first item in xs for best matching predicate.
   Optional function `f` to extract argument for applying predicates to."
  ([preds, xs] (find-priority-pred nil preds xs))
  ([f, preds, xs]
   (let [match-pred (if f
                      (let [match-pred (apply some-fn (map-indexed
                                                        (fn [i pred] #(when (pred (val %))
                                                                        (MapEntry. i (key %))))
                                                        preds))]
                        #(match-pred (MapEntry. % (f %))))

                      (apply some-fn (map-indexed
                                       (fn [i pred] #(when (pred %) (MapEntry. i %)))
                                       preds)))]

     (reduce (fn [old, v]
               (if-some [new (match-pred v)]
                 (let [^int i (key new)]
                   (cond
                     (zero? i) (reduced new)
                     (nil? old) new
                     (< i ^int (key old)) new
                     :else old))
                 old))
             nil xs))))


(comment

  "Optimistic dataset"

  (criterium.core/quick-bench
    (find-group-by dataset1-opt)
    #_[{:text "789", :type "physical"}])
  #_"Execution time mean : 1,551645 µs"

  (criterium.core/quick-bench
    (find-loop dataset1-opt)
    #_"789")
  #_"Execution time mean : 505,074634 ns"

  (criterium.core/quick-bench
    (find-priority-pred [#(= "physical" (:type %))
                         #(= "postal" (:type %))] dataset1-opt)
    #_[0 {:text "789", :type "physical"}])
  #_"Execution time mean : 537,785888 ns"

  (criterium.core/quick-bench
    (find-priority-pred :type [#(= "physical" %)
                               #(= "postal" %)] dataset1-opt)
    #_[0 {:text "789", :type "physical"}])
  #_"Execution time mean : 546,129240 ns"


  "Pessimistic dataset"

  (criterium.core/quick-bench
    (find-group-by dataset1-pes)
    #_[{:text "789", :type "physical"}])
  #_"Execution time mean : 1,195726 µs"

  (criterium.core/quick-bench
    (find-loop dataset1-pes)
    #_"789")
  #_"Execution time mean : 1,674947 µs"

  (criterium.core/quick-bench
    (find-priority-pred [#(= "physical" (:type %))
                         #(= "postal" (:type %))] dataset1-pes)
    #_[0 {:text "789", :type "physical"}])
  #_"Execution time mean : 1,083892 µs"

  (criterium.core/quick-bench
    (find-priority-pred :type [#(= "physical" %)
                               #(= "postal" %)] dataset1-pes)
    #_[0 {:text "789", :type "physical"}])
  #_"Execution time mean : 986,223554 ns"


  "Dataset to match secondary predicate."

  (criterium.core/quick-bench
    (find-group-by dataset2)
    #_[{:text "123", :type "postal"}])
  #_"Execution time mean : 1,579207 µs"

  (criterium.core/quick-bench
    (find-loop dataset2)
    #_"123")
  #_"Execution time mean : 1,648119 µs"

  (criterium.core/quick-bench
    (find-priority-pred [#(= "physical" (:type %))
                         #(= "postal" (:type %))] dataset2)
    #_[1 {:text "123", :type "postal"}])
  #_"Execution time mean : 1,184468 µs"

  (criterium.core/quick-bench
    (find-priority-pred :type [#(= "physical" %)
                               #(= "postal" %)] dataset2)
    #_[1 {:text "123", :type "postal"}])
  #_"Execution time mean : 1,067486 µs"


  "Dataset to match nothing"

  (criterium.core/quick-bench
    (find-group-by dataset3)
    #_nil)
  #_"Execution time mean : 1,587247 µs"

  (criterium.core/quick-bench
    (find-loop dataset3)
    #_nil)
  #_"Execution time mean : 1,701745 µs"

  (criterium.core/quick-bench
    (find-priority-pred [#(= "physical" (:type %))
                         #(= "postal" (:type %))] dataset3)
    #_nil)
  #_"Execution time mean : 1,198476 µs"

  (criterium.core/quick-bench
    (find-priority-pred :type [#(= "physical" %)
                               #(= "postal" %)] dataset3)
    #_nil)
  #_"Execution time mean : 1,099103 µs"

  'comment)