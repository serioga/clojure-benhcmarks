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

(defn- as-char-pred
  [pred]
  (condp instance? pred Character #(.equals ^Character pred %)
                        String,,, #(char-from? ^String pred %)
                        pred))

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn ^String trimr-pred
  "Removes chars from the right side of string by `pred`.
   Accepts string (set of chars) and single char as `pred`."
  [^CharSequence s, pred]
  (let [pred (as-char-pred pred)]
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
  (-> "path" (trimr-pred #(Character/isWhitespace ^Character %))) #_"  8,271516 ns"
  (-> "path" (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_"  8,871528 ns"
  (-> "path" (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_" 10,992364 ns"

  (-> "path   " (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_" 28,298732 ns"
  (-> "path   " (cuerdas/rtrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"871,570969 ns (!)"
  (-> "path   " (trimr-pred #(Character/isWhitespace ^Character %))) #_" 32,343976 ns"
  (-> "path   " (trimr-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_" 31,909357 ns"
  (-> "path   " (trimr-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_" 35,683274 ns"

  (-> "path///" (cuerdas/rtrim "/")),,,,,,,,,,,,,,,,,, #_"839,719600 ns (!)"
  (-> "path///" (trimr-pred #(.equals \/ %))),,,,,,,,, #_" 26,952809 ns"
  (-> "path///" (trimr-pred \/)),,,,,,,,,,,,,,,,,,,,,, #_" 27,103821 ns"
  (-> "path///" (trimr-pred #(= \/ %))),,,,,,,,,,,,,,, #_" 47,585515 ns (!)"
  (-> "path///" (trimr-pred #(char-from? "/" %))),,,,, #_" 35,659722 ns"
  (-> "path///" (trimr-pred (partial char-from? "/"))) #_" 67,514078 ns (!)"
  (-> "path///" (trimr-pred #(slash-char? %))),,,,,,,, #_" 30,286359 ns"
  (-> "path///" (trimr-pred slash-char?)),,,,,,,,,,,,, #_" 30,076203 ns"
  (-> "path///" (trimr-pred "/")),,,,,,,,,,,,,,,,,,,,, #_" 34,213807 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn ^String triml-pred
  "Removes whitespace from the left side of string by `pred`.
   Accepts string (set of chars) and single char as `pred`."
  [^CharSequence s, pred]
  (let [len (.length s)
        pred (as-char-pred pred)]
    (loop [index 0]
      (if (= len index)
        ""
        (if (pred (.charAt s index))
          (recur (unchecked-inc index))
          (.toString (.subSequence s index len)))))))

(comment
  (-> "path" (string/triml)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  5,977752 ns"
  (-> "path" (cuerdas/ltrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"713,505783 ns (!)"
  (-> "path" (triml-pred #(Character/isWhitespace ^Character %))) #_"  8,351322 ns"
  (-> "path" (triml-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_"  8,630335 ns"
  (-> "path" (triml-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_" 10,735374 ns"

  (-> "   path" (string/triml)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_" 28,298732 ns"
  (-> "   path" (cuerdas/ltrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"860,774839 ns (!)"
  (-> "   path" (triml-pred #(Character/isWhitespace ^Character %))) #_" 30,994988 ns"
  (-> "   path" (triml-pred #(whitespace-char? %))),,,,,,,,,,,,,,,,, #_" 32,184716 ns"
  (-> "   path" (triml-pred whitespace-char?)),,,,,,,,,,,,,,,,,,,,,, #_" 33,413751 ns"

  (-> "///path" (cuerdas/ltrim "/")),,,,,,,,,,,,,,,,,, #_"786,394368 ns (!)"
  (-> "///path" (triml-pred #(.equals \/ %))),,,,,,,,, #_" 27,674655 ns"
  (-> "///path" (triml-pred \/)),,,,,,,,,,,,,,,,,,,,,, #_" 27,807344 ns"
  (-> "///path" (triml-pred #(= \/ %))),,,,,,,,,,,,,,, #_" 44,685301 ns (!)"
  (-> "///path" (triml-pred #(char-from? "/" %))),,,,, #_" 33,704091 ns"
  (-> "///path" (triml-pred (partial char-from? "/"))) #_" 65,913243 ns (!)"
  (-> "///path" (triml-pred #(slash-char? %))),,,,,,,, #_" 28,275656 ns"
  (-> "///path" (triml-pred slash-char?)),,,,,,,,,,,,, #_" 28,819234 ns"
  (-> "///path" (triml-pred "/")),,,,,,,,,,,,,,,,,,,,, #_" 32,865040 ns")
