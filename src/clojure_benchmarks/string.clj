(ns clojure-benchmarks.string
  (:require [clojure.string :as string]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(comment ; String equality
  (.equals "123" "123"), ;  3,649373 ns
  (.equals "123" "1234") ;  3,647758 ns
  (.equals "123" "456"), ;  6,092860 ns
  (.equals "1234" nil),, ;  3,661969 ns
  (= "123" "123"),,,,,,, ;  3,661472 ns
  (= "123" "1234"),,,,,, ; 22,036613 ns
  #_'comment)

(comment ; String equality ignore case
  (.equalsIgnoreCase "ok" "OK"),,,,,,,,,, ; 19,801496 ns
  (.equals "ok" (.toLowerCase "OK")),,,,, ; 45,061715 ns
  (.equals "ok" (string/lower-case "OK")) ; 45,193935 ns
  #_'comment)

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn- whitespace-char? [^Character c] (Character/isWhitespace c))
(defn- char-from? [^String s, ^Character c] (not (neg? (.indexOf s (int c)))))

(comment
  (Character/isWhitespace \newline) ; 4,235602 ns
  (whitespace-char? \newline),,,,,, ; 4,959288 ns
  (char-from? "\n",,, \newline),,,, ; 6,010373 ns
  (char-from? " \r\n" \newline),,,, ; 6,287797 ns
  #_'comment)

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn ^String trimr-pred
  "Removes chars from the right side of string by `pred`."
  [^CharSequence s, pred]
  (loop [i (.length s)]
    (if (zero? i)
      ""
      (if (pred (.charAt s (unchecked-dec i)))
        (recur (unchecked-dec i))
        (.toString (.subSequence s 0 i))))))

(defn- slash-char? [c] (char-from? "/" c))

(comment
  (-> "path" (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, ;  5,408585 ns
  (-> "path" (trimr-pred #(Character/isWhitespace ^Character %))) ;  5,425954 ns
  (-> "path" (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, ;  6,307924 ns
  (-> "path" (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, ; 11,562080 ns

  (-> "path   " (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, ; 27,588813 ns
  (-> "path   " (trimr-pred #(Character/isWhitespace ^Character %))) ; 25,308149 ns
  (-> "path   " (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, ; 27,309861 ns
  (-> "path   " (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, ; 48,758966 ns

  (-> "path///" (trimr-pred #(char-from? "/" %))),,,,, ; 30,334351 ns
  (-> "path///" (trimr-pred (partial char-from? "/"))) ; 65,495619 ns
  (-> "path///" (trimr-pred #(slash-char? %))),,,,,,,, ; 31,863251 ns
  (-> "path///" (trimr-pred slash-char?)),,,,,,,,,,,,, ; 57,319882 ns
  #_'comment)

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
