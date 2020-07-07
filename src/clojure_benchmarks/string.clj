(ns clojure-benchmarks.string
  (:require [clojure.string :as string]
            [cuerdas.core :as cuerdas]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

;; String equality
(comment
  (.equals "123" "123"), #_" 3,649373 ns"
  (.equals "123" "1234") #_" 3,647758 ns"
  (.equals "123" "456"), #_" 6,092860 ns"
  (.equals "1234" nil),, #_" 3,661969 ns"
  (= "123" "123"),,,,,,, #_" 3,661472 ns"
  (= "123" "1234"),,,,,, #_"22,036613 ns")

;; String equality ignore case
(comment
  (.equalsIgnoreCase "ok" "OK"),,,,,,,,,, #_"19,801496 ns"
  (.equals "ok" (.toLowerCase "OK")),,,,, #_"45,061715 ns"
  (.equals "ok" (string/lower-case "OK")) #_"45,193935 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn- whitespace-char? [^Character c] (Character/isWhitespace c))
(defn- char-from? [^String s, ^Character c] (not (neg? (.indexOf s (int c)))))

(comment
  (Character/isWhitespace \newline) #_"4,235602 ns"
  (whitespace-char? \newline),,,,,, #_"4,959288 ns"
  (char-from? "\n",,, \newline),,,, #_"6,010373 ns"
  (char-from? " \r\n" \newline),,,, #_"6,287797 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

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
  (-> "path" (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  5,408585 ns"
  (-> "path" (cuerdas/rtrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"712,637256 ns"
  (-> "path" (trimr-pred #(Character/isWhitespace ^Character %))) #_"  5,425954 ns"
  (-> "path" (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_"  6,307924 ns"
  (-> "path" (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_" 11,562080 ns"

  (-> "path   " (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_" 27,588813 ns"
  (-> "path   " (cuerdas/rtrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"849,094234 ns"
  (-> "path   " (trimr-pred #(Character/isWhitespace ^Character %))) #_" 25,308149 ns"
  (-> "path   " (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_" 27,309861 ns"
  (-> "path   " (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_" 48,758966 ns"

  (-> "path///" (cuerdas/rtrim "/")),,,,,,,,,,,,,,,,,, #_"851,435102 ns"
  (-> "path///" (trimr-pred #(.equals \/ %))),,,,,,,,, #_" 26,033381 ns"
  (-> "path///" (trimr-pred #(= \/ %))),,,,,,,,,,,,,,, #_" 41,624514 ns"
  (-> "path///" (trimr-pred #(char-from? "/" %))),,,,, #_" 30,334351 ns"
  (-> "path///" (trimr-pred (partial char-from? "/"))) #_" 65,495619 ns"
  (-> "path///" (trimr-pred #(slash-char? %))),,,,,,,, #_" 31,863251 ns"
  (-> "path///" (trimr-pred slash-char?)),,,,,,,,,,,,, #_" 57,319882 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
