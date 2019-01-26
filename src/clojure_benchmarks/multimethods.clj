(ns clojure-benchmarks.multimethods
  (:require
    [clojure.core.match :refer [match]]
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)

;[org.clojure/clojure "1.10.0"]
;[criterium "0.4.4"]
;[org.clojure/core.match "0.3.0-alpha5"]

;jdk1.8.0_152

;OS Name:                   Microsoft Windows 10 Pro
;OS Version:                10.0.17134 N/A Build 17134
;OS Configuration:          Standalone Workstation
;OS Build Type:             Multiprocessor Free
;System Manufacturer:       FUJITSU
;System Model:              CELSIUS W510
;System Type:               x64-based PC
;Processor(s):              1 Processor(s) Installed.
;                           [01]: Intel64 Family 6 Model 42 Stepping 7 GenuineIntel ~1600 Mhz
;Total Physical Memory:     16 254 MB
;Available Physical Memory: 5 950 MB


(defmulti test-multi
  "Dispatch using multimethods."
  :type)

(defmethod test-multi :a [m] m)
(defmethod test-multi :b [m] m)
(defmethod test-multi :c [m] m)
(defmethod test-multi :d [m] m)
(defmethod test-multi :e [m] m)

(comment

  (criterium/quick-bench
    (test-multi {:type :a}))
  ;Evaluation count : 18999216 in 6 samples of 3166536 calls.
  ;             Execution time mean : 30,240010 ns
  ;    Execution time std-deviation : 0,692414 ns
  ;   Execution time lower quantile : 29,876592 ns ( 2,5%)
  ;   Execution time upper quantile : 31,433536 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  (criterium/quick-bench
    (test-multi {:type :e}))
  ;Evaluation count : 19173192 in 6 samples of 3195532 calls.
  ;             Execution time mean : 29,847402 ns
  ;    Execution time std-deviation : 0,287146 ns
  ;   Execution time lower quantile : 29,520729 ns ( 2,5%)
  ;   Execution time upper quantile : 30,266948 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  nil)


(defn test-type-a [m] m)
(defn test-type-b [m] m)
(defn test-type-c [m] m)
(defn test-type-d [m] m)
(defn test-type-e [m] m)


(defn test-condp
  "Dispatch using `condp`."
  [m]
  (condp = (:type m)
    :a (test-type-a m)
    :b (test-type-b m)
    :c (test-type-c m)
    :d (test-type-d m)
    :e (test-type-e m)))

(comment

  (criterium/quick-bench
    (test-condp {:type :a}))
  ;Evaluation count : 43704552 in 6 samples of 7284092 calls.
  ;             Execution time mean : 11,954346 ns
  ;    Execution time std-deviation : 0,143214 ns
  ;   Execution time lower quantile : 11,791436 ns ( 2,5%)
  ;   Execution time upper quantile : 12,103464 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  (criterium/quick-bench
    (test-condp {:type :e}))
  ;Evaluation count : 3271104 in 6 samples of 545184 calls.
  ;             Execution time mean : 184,493089 ns
  ;    Execution time std-deviation : 4,099833 ns
  ;   Execution time lower quantile : 181,131451 ns ( 2,5%)
  ;   Execution time upper quantile : 190,663945 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  nil)


(defn test-case
  "Dispatch using `case`."
  [m]
  (case (:type m)
    :a (test-type-a m)
    :b (test-type-b m)
    :c (test-type-c m)
    :d (test-type-d m)
    :e (test-type-e m)))

(comment

  (criterium/quick-bench
    (test-case {:type :a}))
  ;Evaluation count : 22287360 in 6 samples of 3714560 calls.
  ;             Execution time mean : 25,310597 ns
  ;    Execution time std-deviation : 0,340535 ns
  ;   Execution time lower quantile : 24,928553 ns ( 2,5%)
  ;   Execution time upper quantile : 25,762918 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  (criterium/quick-bench
    (test-case {:type :e}))
  ;Evaluation count : 22458456 in 6 samples of 3743076 calls.
  ;             Execution time mean : 25,148748 ns
  ;    Execution time std-deviation : 0,246338 ns
  ;   Execution time lower quantile : 24,880954 ns ( 2,5%)
  ;   Execution time upper quantile : 25,415390 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  nil)


(defn test-map
  "Dispatch using map of functions."
  [m]
  (let [dispatch {:a test-type-a
                  :b test-type-b
                  :c test-type-c
                  :d test-type-d
                  :e test-type-e}
        f (get dispatch (:type m))]
    (f m)))

(comment

  (criterium/quick-bench
    (test-map {:type :a}))
  ;Evaluation count : 19487130 in 6 samples of 3247855 calls.
  ;             Execution time mean : 29,273969 ns
  ;    Execution time std-deviation : 0,185716 ns
  ;   Execution time lower quantile : 29,023084 ns ( 2,5%)
  ;   Execution time upper quantile : 29,491518 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  (criterium/quick-bench
    (test-map {:type :e}))
  ;Evaluation count : 17748264 in 6 samples of 2958044 calls.
  ;             Execution time mean : 32,169376 ns
  ;    Execution time std-deviation : 0,251097 ns
  ;   Execution time lower quantile : 32,012998 ns ( 2,5%)
  ;   Execution time upper quantile : 32,595155 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  nil)


(defn test-match
  "Dispatch using `clojure.core.match`."
  [m]
  (match [(:type m)]
    [:a] (test-type-a m)
    [:b] (test-type-b m)
    [:c] (test-type-c m)
    [:d] (test-type-d m)
    [:e] (test-type-e m)))

(comment

  (criterium/quick-bench
    (test-match {:type :a}))
  ;Evaluation count : 31569144 in 6 samples of 5261524 calls.
  ;             Execution time mean : 17,252350 ns
  ;    Execution time std-deviation : 0,086253 ns
  ;   Execution time lower quantile : 17,094931 ns ( 2,5%)
  ;   Execution time upper quantile : 17,346953 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  (criterium/quick-bench
    (test-match {:type :e}))
  ;Evaluation count : 2899164 in 6 samples of 483194 calls.
  ;             Execution time mean : 205,341057 ns
  ;    Execution time std-deviation : 0,692279 ns
  ;   Execution time lower quantile : 204,665762 ns ( 2,5%)
  ;   Execution time upper quantile : 206,379073 ns (97,5%)
  ;                   Overhead used : 1,779846 ns

  nil)
