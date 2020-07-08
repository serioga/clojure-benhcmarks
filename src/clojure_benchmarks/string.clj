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
  (Character/isWhitespace \newline) #_"4,785873 ns"
  (whitespace-char? \newline),,,,,, #_"5,291325 ns"
  (char-from? "\n",,, \newline),,,, #_"6,031422 ns"
  (char-from? " \r\n" \newline),,,, #_"6,680646 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn ^String trimr-pred
  "Removes chars from the right side of string by `pred`.
   Accepts string (set of chars) and single char as `pred`."
  [^CharSequence s, pred]
  (let [pred (condp instance? pred Character #(.equals ^Character pred %)
                                   String,,, #(char-from? ^String pred %)
                                   pred)]
    (loop [i (.length s)]
      (if (zero? i)
        ""
        (if (pred (.charAt s (unchecked-dec i)))
          (recur (unchecked-dec i))
          (.toString (.subSequence s 0 i)))))))

(defn- slash-char? [c] (.equals \/ c))

(comment
  (-> "path" (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  5,892719 ns"
  (-> "path" (cuerdas/rtrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"718,482296 ns (!)"
  (-> "path" (trimr-pred #(Character/isWhitespace ^Character %))) #_"  7,270472 ns"
  (-> "path" (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_"  7,883876 ns"
  (-> "path" (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_"  9,268724 ns"

  (-> "path   " (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_" 28,298732 ns"
  (-> "path   " (cuerdas/rtrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"871,570969 ns (!)"
  (-> "path   " (trimr-pred #(Character/isWhitespace ^Character %))) #_" 30,829851 ns"
  (-> "path   " (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_" 31,731720 ns"
  (-> "path   " (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_" 33,413751 ns"

  (-> "path///" (cuerdas/rtrim "/")),,,,,,,,,,,,,,,,,, #_"839,719600 ns (!)"
  (-> "path///" (trimr-pred #(.equals \/ %))),,,,,,,,, #_" 26,754372 ns"
  (-> "path///" (trimr-pred \/)),,,,,,,,,,,,,,,,,,,,,, #_" 26,514774 ns"
  (-> "path///" (trimr-pred #(= \/ %))),,,,,,,,,,,,,,, #_" 45,278277 ns (!)"
  (-> "path///" (trimr-pred #(char-from? "/" %))),,,,, #_" 34,791646 ns"
  (-> "path///" (trimr-pred (partial char-from? "/"))) #_" 64,455251 ns (!)"
  (-> "path///" (trimr-pred #(slash-char? %))),,,,,,,, #_" 29,866398 ns"
  (-> "path///" (trimr-pred slash-char?)),,,,,,,,,,,,, #_" 31,433266 ns"
  (-> "path///" (trimr-pred "/")),,,,,,,,,,,,,,,,,,,,, #_" 33,470413 ns")


;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
