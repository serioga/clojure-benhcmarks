(ns clojure-benchmarks.long-adder
  "https://t.me/clojure_ru/103864"
  (:require
    [clojure.core.async :as async]
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)


(defn count-down-latch-chan
  "https://twitter.com/cgrand/status/801377579726962688"
  [n]
  (async/chan 1 (comp (drop (dec n)) (take 1))))


(comment
  (.. Runtime getRuntime availableProcessors)
  ;=> 8

  ; No parallelism (single pass) ==================================

  ; LongAdder, no parallelism
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))]
      (dotimes [_ parallelism]
        (.add acc adding))
      (.sum acc)))
  ; Execution time mean : 10,959324 µs

  ; atom, no parallelism
  (criterium/quick-bench
    (let [acc (atom (long 0))
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))]
      (dotimes [_ parallelism]
        (swap! acc unchecked-add adding))
      (deref acc)))
  ; Execution time mean : 19,566966 µs, 2× slower than LongAdder

  ; volatile, no parallelism
  (criterium/quick-bench
    (let [acc (volatile! (long 0))
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))]
      (dotimes [_ parallelism]
        (vswap! acc unchecked-add adding))
      (deref acc)))
  ; Execution time mean : 9,657035 µs, 10% faster than LongAdder

  ; loop without mutation
  (criterium/quick-bench
    (let [limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))]
      (loop [acc (long 0)
             n parallelism]
        (if (pos? n)
          (recur (unchecked-add acc adding) (unchecked-dec n))
          acc))))
  ; Execution time mean : 419,329872 ns, 260× faster than LongAdder (!)


  ; Parallelism ======================================================

  ; LongAdder + CountDownLatch + core.async
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          done-signal (java.util.concurrent.CountDownLatch. parallelism)]
      (dotimes [_ parallelism]
        (async/go
          (.add acc adding)
          (.countDown done-signal)
          :done))
      (.await done-signal)
      (.sum acc)))
  ; Execution time mean : 499,350403 µs, 45× slower than without parallelism

  ; LongAdder + CountDownLatch + futures
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          done-signal (java.util.concurrent.CountDownLatch. parallelism)]
      (dotimes [_ parallelism]
        (future
          (.add acc adding)
          (.countDown done-signal)
          :done))
      (.await done-signal)
      (.sum acc)))
  ; Execution time mean : 788,807123 µs, 58% slower than with core.async

  ; LongAdder + Phaser + core.async
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          done-signal (java.util.concurrent.Phaser. parallelism)]
      (dotimes [_ parallelism]
        (async/go
          (.add acc adding)
          (.arriveAndDeregister done-signal)
          :done))
      (.awaitAdvance done-signal 0)
      (.sum acc)))
  ; Execution time mean : 498,135672 µs, slightly faster than with CountDownLatch

  ; atom + CountDownLatch + core.async
  (criterium/quick-bench
    (let [acc (atom (long 0))
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          done-signal (java.util.concurrent.CountDownLatch. parallelism)]
      (dotimes [_ parallelism]
        (async/go
          (swap! acc unchecked-add adding)
          (.countDown done-signal)
          :done))
      (.await done-signal)
      (deref acc)))
  ; Execution time mean : 533,165943 µs, 6% slower than LongAdder

  ; LongAdder + promise + core.async (buggy due to possible race condition)
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          done (promise)
          count-run (let [c (atom (long (dec parallelism)))]
                      (fn []
                        (when (zero? (swap! c unchecked-dec))
                          (deliver done :done))))]

      (dotimes [_ parallelism]
        (async/go
          (.add acc adding)
          (count-run)
          :done))
      (deref done)
      (.sum acc)))
  ; Execution time mean : 533,165943 µs

  ; LongAdder + async pipeline
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          from (async/to-chan (repeat parallelism
                                (fn [] (.add acc adding) :done)))]
      (async/<!!
        (async/pipeline 10
          (async/chan (async/dropping-buffer 1))
          (map #(%)) from))
      (.sum acc)))
  ; Execution time mean : 7,458066 ms

  ; LongAdder + count-down-latch-chan + core.async
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          done (count-down-latch-chan parallelism)]
      (dotimes [_ parallelism]
        (async/go
          (.add acc adding)
          (async/>! done true)))
      (async/<!! done)
      (.sum acc)))
  ; Execution time mean : 1,508173 ms, 3× slower than with CountDownLatch

  ; LongAdder + sync chan + core.async
  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          sync (async/chan parallelism)]

      (dotimes [_ parallelism]
        (async/go
          (.add acc adding)
          (async/>! sync true)))

      (dotimes [_ parallelism]
        (async/<!! sync))

      (.sum acc)))
  ; Execution time mean : 1,414124 ms

  :end)

(defn cpu-bound-sum
  [n]
  (loop [i (int 0)
         n (int n)]
    (if (pos? n)
      (recur (unchecked-inc i) (unchecked-dec n))
      i)))

(comment
  (criterium/quick-bench
    (cpu-bound-sum 100000))
  ; 33,535895 µs

  (criterium/quick-bench
    (loop [i (int 0)
           n (int 1e8)]
      (if (pos? n)
        (recur (unchecked-inc i) (unchecked-dec n))
        i)))
  ; 59,182515 ms

  (criterium/quick-bench
    (loop [i (int 0)
           n (int 1000)]
      (if (pos? n)
        (recur (unchecked-add i (cpu-bound-sum 100000)) (unchecked-dec n))
        i)))
  ; 29,806815 ms

  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))]
      (dotimes [_ parallelism]
        (.add acc (cpu-bound-sum adding)))
      (.sum acc)))
  ; 60,477632 ms

  (criterium/quick-bench
    (let [acc (java.util.concurrent.atomic.LongAdder.)
          limit 1e8
          parallelism 1000
          adding (long (/ limit parallelism))
          done-signal (java.util.concurrent.CountDownLatch. parallelism)]
      (dotimes [_ parallelism]
        (async/go
          (.add acc (cpu-bound-sum adding))
          (.countDown done-signal)
          :done))
      (.await done-signal)
      (.sum acc)))
  ; 7,178148 ms

  :end)
