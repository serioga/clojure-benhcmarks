(ns clojure-benchmarks.url
  "URL manipulation."
  (:require 
    [clojure.string :as string])
  (:import
    (clojure.lang MapEntry)
    (java.net URI URL URLDecoder)))

(set! *warn-on-reflection* true)


(defn parse-url-data
  [url]
  (let [u (URL. url)]
    {:url/protocol (.getProtocol u)
     :url/host (.getHost u)
     :url/port (let [p (.getPort u)] (when-not (== p -1) p))
     :url/file (.getFile u)
     :url/path (.getPath u)
     :url/query (.getQuery u)
     :url/fragment (.getRef u)}))


(defn url-data->str
  [{:url/keys [protocol host port path query fragment]}]
  (str (URI. protocol nil host (or port -1) path query fragment)))


(defn query-string->map
  ([s] (query-string->map s, identity))
  ([s, key-fn]
   (transduce (map #(let [kv (string/split % #"=" 2)]
                      (MapEntry/create
                        (key-fn (URLDecoder/decode (kv 0)))
                        (URLDecoder/decode (get kv 1 "")))))
     conj {} (some-> s
               (string/split #"&")))))


(comment

  (criterium.core/quick-bench
    (url-data->str
      (parse-url-data "https://test.me/akzx/66617beb?x=1&y=2#xxx"))
    #_"https://test.me/akzx/66617beb?x=1&y=2#xxx")
  #_"Execution time mean : 1,068260 µs"

  (criterium.core/quick-bench
    (query-string->map "x=1&y=2")
    #_{"x" "1", "y" "2"})
  #_"Execution time mean : 887,818614 ns"

  (criterium.core/quick-bench
    (query-string->map "x=1&y=2" keyword)
    #_{:x "1", :y "2"})
  #_"Execution time mean : 905,466950 ns"

  (criterium.core/quick-bench
    (transduce (map #(let [kv (string/split % #"=" 2)]
                       (MapEntry/create
                         (URLDecoder/decode (kv 0))
                         (URLDecoder/decode (get kv 1 "")))))
      conj {} (some-> "x=1&y=2"
                (string/split #"&")))
    #_{"x" "1", "y" "2"})
  #_"Execution time mean : 859,533138 ns"

  (criterium.core/quick-bench
    (transduce (map (fn
                      [part]
                      (let [[k v] (string/split part #"=")]
                        (MapEntry/create (URLDecoder/decode k) (URLDecoder/decode v)))))
      conj {} (some-> "x=1&y=2"
                (string/split #"&")))
    #_{"x" "1", "y" "2"})
  #_"Execution time mean : 952,316967 ns"

  (criterium.core/quick-bench
    (transduce (map #(string/split % #"="))
      conj {} (string/split "x=1&y=2" #"&"))
    #_{"x" "1", "y" "2"})
  #_"Execution time mean : 845,220944 ns"

  (criterium.core/quick-bench
    (into {}
      (map #(string/split % #"="))
      (string/split "x=1&y=2" #"&"))
    #_{"x" "1", "y" "2"})
  #_"Execution time mean : 924,058653 ns"

  (criterium.core/quick-bench
    (into {}
      (some-> "x=1&y=2"
        (string/split #"&")
        (->> (map #(string/split % #"=")))))
    #_{"x" "1", "y" "2"})
  #_"Execution time mean : 1,435830 µs"

  (criterium.core/quick-bench
    (transduce (map #(string/split % #"="))
      conj {} (string/split "a=a&b=a&c=a&d=a&e=a" #"&"))
    #_{"a" "a", "b" "a", "c" "a", "d" "a", "e" "a"})
  #_"Execution time mean : 1,951230 µs"

  (criterium.core/quick-bench
    (into {}
      (map #(string/split % #"="))
      (string/split "a=a&b=a&c=a&d=a&e=a" #"&"))
    #_{"a" "a", "b" "a", "c" "a", "d" "a", "e" "a"})
  #_"Execution time mean : 2,332784 µs"

  (criterium.core/quick-bench
    (parse-url-data "https://test.me/akzx/66617beb?x=1&y=2"))
  #_"Execution time mean : 276,869210 ns"
  
  (criterium.core/quick-bench
    (URI. "https://test.me/akzx/66617beb?x=1&y=2"))
  #_"Execution time mean : 318,418939 ns"
  
  (criterium.core/quick-bench
    (URL. "https://test.me/akzx/66617beb?x=1&y=2"))
  #_"Execution time mean : 259,399222 ns"


  'comment)
