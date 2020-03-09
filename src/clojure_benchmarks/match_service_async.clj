(ns clojure-benchmarks.match-service-async
  "https://t.me/clojure_ru/104646

   Usage:
   ```
    (start-service! (dummy-handler) (dummy-result-chan))

    (add-service-task 1)
    (remove-service-task 1)

    (stop-service!)
   ```"

  (:require
    [clojure.core.async :as async]))

(set! *warn-on-reflection* true)


(def parallelism
  "Max amount of task processed in parallel."
  10)


(def work-buffer-size
  "Max amount of active tasks to process."
  1000)


(defonce
  ^{:doc "Service input channel"
    :private true}
  service (atom nil))


(declare init-service)


(defn stop-service!
  []
  (some-> @service async/close!)
  (reset! service nil))


(defn start-service!
  "Initialize service with task handler `handle-task` and channel `result-chan` to receive results.
   Stores service channel in global state."
  [handle-task, result-chan]
  (stop-service!)
  (reset! service
          (init-service handle-task, result-chan)))


(defn add-service-task
  [task]
  (some-> @service
          (async/put! {:event/command :command/add-task
                       :event/task task})))


(defn remove-service-task
  [task]
  (some-> @service
          (async/put! {:event/command :command/remove-task
                       :event/task task})))


(defn ^:private init-service
  "Initialize service with task handler `handle-task` and channel `result-chan` to receive results."
  [handle-task, result-chan]
  (let [service (async/chan)
        work (async/chan work-buffer-size)]

    (async/go-loop [active-tasks #{}]
      (if-some [{:event/keys [command task] :as event} (async/<! service)]
        (recur
          (case command
            :command/add-task (let [new-task? (not (contains? active-tasks task))]
                                (cond
                                  new-task? (do (println "Task added:" task)
                                                (async/>! work task)
                                                (conj active-tasks task))
                                  :else active-tasks))

            :command/remove-task (disj active-tasks task)

            :command/deliver-task-result (do (async/put! result-chan [task (:event/task-result event)])
                                             (when (contains? active-tasks task)
                                               (async/>! work task))
                                             active-tasks)
            (do (println "[WARN]" "Skip unknown command" command)
                active-tasks)))

        #_"else (channel service is closed)"
        (do (println "Close work channel")
            (async/close! work))))

    (async/pipeline-blocking
      parallelism
      #_to service
      (map (fn
             [task]
             (let [result (try
                            (handle-task task)
                            (catch Throwable ex
                              ex))]
               {:event/command :command/deliver-task-result
                :event/task task
                :event/task-result result})))
      #_from work
      #_close? true)

    service))


(defn dummy-handler
  []
  (fn dummy-handle-task
    [task]
    (let [thread (.getName (Thread/currentThread))]
      (println "[START]" 'dummy-handle-task thread task)
      (Thread/sleep (+ 1000 (rand-int 1000)))
      (println "[DONE]" 'dummy-handle-task thread task))
    "OK"))


(defn dummy-result-chan
  []
  (async/chan (async/dropping-buffer 10)
              (map (fn dummy-handle-result [[task result]]
                     (println "[DONE]" 'dummy-result-chan task "->" result)
                     ""))))


(comment
  (start-service! (dummy-handler) (dummy-result-chan))
  (stop-service!)

  (add-service-task :task/a)
  (remove-service-task :task/a)
  (add-service-task :task/b)
  (remove-service-task :task/b)
  (add-service-task :task/c)
  (remove-service-task :task/c))
