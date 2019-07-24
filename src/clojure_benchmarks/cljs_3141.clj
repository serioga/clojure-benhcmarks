(ns clojure-benchmarks.cljs-3141
  "https://github.com/clojure/clojurescript/commit/63cddf1d1cee375e6b3fe0d3c853631731fe15bc"
  (:require
    [criterium.core :as criterium]))

(set! *warn-on-reflection* true)


(def chars64 "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/")
(def char->int (zipmap chars64 (range 0 64)))
(def int->char (zipmap (range 0 64) chars64))

(defn encode-map-orig [n]
  (let [e (find int->char n)]
    (if e
      (second e)
      (throw (Error. (str "Must be between 0 and 63: " n))))))

(defn encode-case-orig [^long n]
  (case n
    0 \A 1 \B 2 \C 3 \D 4 \E 5 \F 6 \G 7 \H 8 \I 9 \J 10 \K 11 \L 12 \M
    13 \N 14 \O 15 \P 16 \Q 17 \R 18 \S 19 \T 20 \U 21 \V 22 \W 23 \X 24 \Y 25 \Z
    26 \a 27 \b 28 \c 29 \d 30 \e 31 \f 32 \g 33 \h 34 \i 35 \j 36 \k 37 \l 38 \m
    39 \n 40 \o 41 \p 42 \q 43 \r 44 \s 45 \t 46 \u 47 \v 48 \w 49 \x 50 \y 51 \z
    52 \0 53 \1 54 \2 55 \3 56 \4 57 \5 58 \6 59 \7 60 \8 61 \9 62 \+ 63 \/
    (throw (Error. (str "Must be between 0 and 63: " n)))))

(defn encode-map-simple [n]
  (or (int->char n)
    (throw (Error. (str "Must be between 0 and 63: " n)))))

(def chars64vec (vec chars64))

(defn encode-vec [^long n]
  (chars64vec n))

(def chars64arr (char-array (count chars64) (vec chars64)))

(defn encode-char-array [^long n]
  (aget ^chars chars64arr n))

#_(comment
    (find int->char 10)
    (encode-map-orig 10)
    (criterium/quick-bench
      (encode-map-orig 10))
    ; Execution time mean : 92,771802 ns

    (encode-map-simple 10)
    (criterium/quick-bench
      (encode-map-simple 10))
    ; Execution time mean : 39,404491 ns (2.3x faster)

    (encode-vec 10)
    (criterium/quick-bench
      (encode-vec 10))
    ; Execution time mean : 5,535958 ns (16x faster)

    (encode-case-orig 10)
    (criterium/quick-bench
      (encode-case-orig 10))
    ; Execution time mean : 4,380188 ns (21x faster)

    (encode-char-array 10)
    (criterium/quick-bench
      (encode-char-array 10))
    ; Execution time mean : 4,043630 ns (22x faster)

    nil)
