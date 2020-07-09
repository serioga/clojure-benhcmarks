(ns clojure-benchmarks.string
  (:require [clojure.string :as string]
            [clojure.test :as t]
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

(defn char-of?
  "True if char `c` belongs to the set of chars of the string `s`."
  [^String s, ^Character c]
  (not (neg? (.indexOf s (int c)))))

(defn char-digit?
  "True if char `c` is digit."
  [^Character c]
  (Character/isDigit c))

(defn char-whitespace?
  "True if `c` is whitespace character."
  [^Character c]
  (Character/isWhitespace c))

(comment
  (Character/isWhitespace \newline) #_"4,785873 ns"
  (char-whitespace? \newline),,,,,, #_"5,291325 ns"
  (char-of? "\n",,, \newline),,,,,, #_"6,031422 ns"
  (char-of? " \r\n" \newline),,,,,, #_"6,680646 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn- fn'char-equals? [test] (fn [c] (.equals ^Character test c)))
(defn- fn'char-of?,,,, [test] (fn [c] (char-of? test c)))

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn ^String trimr-pred
  "Removes chars from the right side of string by `pred`.
   The `pred` is a predicate function for chars to be removed."
  [^CharSequence s, pred]
  (loop [i (.length s)]
    (if (zero? i)
      ""
      (if (pred (.charAt s (unchecked-dec i)))
        (recur (unchecked-dec i))
        (.toString (.subSequence s 0 i))))))

(defn trimr-char
  "Remove char `c` from the right side of string `s`."
  {:test   (fn [] (t/are [expected actual] (= expected actual)
                    "path" (trimr-char "path",,, \/) #_" 7 ns"
                    "path" (trimr-char "path///" \/) #_"30 ns"))
   :inline (fn [s c] `(trimr-pred ~s (fn'char-equals? ~c)))}
  [s, c]
  (trimr-pred s (fn'char-equals? c)))

(defn trimr-chars
  "Remove chars of `cs` from the right side of string `s`."
  {:test   (fn [] (t/are [expected actual] (= expected actual)
                    "path" (trimr-chars "path",,, "/") #_" 9 ns"
                    "path" (trimr-chars "path///" "/") #_"33 ns"))
   :inline (fn [s cs] `(trimr-pred ~s (fn'char-of? ~cs)))}
  [s, cs]
  (trimr-pred s (fn'char-of? cs)))

(defn- slash-char? [c] (.equals \/ c))

(comment
  (-> "path" (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  5,892719 ns"
  (-> "path" (cuerdas/rtrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"718,482296 ns (!)"
  (-> "path" (trimr-pred #(Character/isWhitespace ^Character %))) #_"  6,567978 ns"
  (-> "path" (trimr-pred #(char-whitespace? %))),,,,,,,,,,,,,,,,, #_"  6,885543 ns"
  (-> "path" (trimr-pred char-whitespace?)),,,,,,,,,,,,,,,,,,,,,, #_"  7,525092 ns"
  (-> "path" (trimr-char \/)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  6,636071 ns"
  (-> "path" (trimr-chars "/")),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  8,128862 ns"

  (-> "path   " (string/trimr)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_" 28,298732 ns"
  (-> "path   " (cuerdas/rtrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"871,570969 ns (!)"
  (-> "path   " (trimr-pred #(Character/isWhitespace ^Character %))) #_" 30,934328 ns"
  (-> "path   " (trimr-pred #(char-whitespace? %))),,,,,,,,,,,,,,,,, #_" 30,658976 ns"
  (-> "path   " (trimr-pred char-whitespace?)),,,,,,,,,,,,,,,,,,,,,, #_" 31,797840 ns"

  (-> "path///" (cuerdas/rtrim "/")),,,,,,,,,,,,,,,, #_"839,719600 ns (!)"
  (-> "path///" (trimr-pred #(.equals \/ %))),,,,,,, #_" 25,736332 ns"
  (-> "path///" (trimr-char \/)),,,,,,,,,,,,,,,,,,,, #_" 26,742848 ns"
  (-> "path///" (trimr-pred #(= \/ %))),,,,,,,,,,,,, #_" 45,894650 ns (!)"
  (-> "path///" (trimr-pred #(slash-char? %))),,,,,, #_" 29,041612 ns"
  (-> "path///" (trimr-pred slash-char?)),,,,,,,,,,, #_" 27,016564 ns"
  (-> "path///" (trimr-pred #(char-of? "/" %))),,,,, #_" 35,659722 ns"
  (-> "path///" (trimr-pred (partial char-of? "/"))) #_" 67,514078 ns (!)"
  (-> "path///" (trimr-chars "/")),,,,,,,,,,,,,,,,,, #_" 33,346595 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn ^String triml-pred
  "Removes chars from the left side of string by `pred`.
   The `pred` is a predicate function for chars to be removed."
  {:test (fn [] (t/are [expected actual] (= expected actual)
                  "path" (triml-pred "path",,, char-whitespace?) #_" 7 ns"
                  "path" (triml-pred "   path" char-whitespace?) #_"30 ns"))}
  [^CharSequence s, pred]
  (let [len (.length s)]
    (loop [index 0]
      (if (= len index)
        ""
        (if (pred (.charAt s index))
          (recur (unchecked-inc index))
          (.toString (.subSequence s index len)))))))

(defn triml-char
  "Remove char `c` from the left side of string `s`."
  {:test   (fn [] (t/are [expected actual] (= expected actual)
                    "path" (triml-char "path",,, \/) #_" 7 ns"
                    "path" (triml-char "///path" \/) #_"30 ns"))
   :inline (fn [s c] `(triml-pred ~s (fn'char-equals? ~c)))}
  [s, c]
  (triml-pred s (fn'char-equals? c)))

(defn triml-chars
  "Remove chars of `cs` from the left side of string `s`."
  {:test   (fn [] (t/are [expected actual] (= expected actual)
                    "path" (triml-chars "path",,, "/") #_" 9 ns"
                    "path" (triml-chars "///path" "/") #_"33 ns"))
   :inline (fn [s cs] `(triml-pred ~s (fn'char-of? ~cs)))}
  [s, cs]
  (triml-pred s (fn'char-of? cs)))

(comment
  (-> "path" (string/triml)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  5,977752 ns"
  (-> "path" (cuerdas/ltrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"713,505783 ns (!)"
  (-> "path" (triml-pred #(Character/isWhitespace ^Character %))) #_"  6,666765 ns"
  (-> "path" (triml-pred #(char-whitespace? %))),,,,,,,,,,,,,,,,, #_"  6,992608 ns"
  (-> "path" (triml-pred char-whitespace?)),,,,,,,,,,,,,,,,,,,,,, #_"  7,230147 ns"
  (-> "path" (triml-char \/)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  6,636071 ns"
  (-> "path" (triml-chars "/")),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"  8,690337 ns"

  (-> "   path" (string/triml)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_" 28,298732 ns"
  (-> "   path" (cuerdas/ltrim)),,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, #_"860,774839 ns (!)"
  (-> "   path" (triml-pred #(Character/isWhitespace ^Character %))) #_" 29,316102 ns"
  (-> "   path" (triml-pred #(char-whitespace? %))),,,,,,,,,,,,,,,,, #_" 30,535491 ns"
  (-> "   path" (triml-pred char-whitespace?)),,,,,,,,,,,,,,,,,,,,,, #_" 31,186980 ns"

  (-> "///path" (cuerdas/ltrim "/")),,,,,,,,,,,,,,,, #_"786,394368 ns (!)"
  (-> "///path" (triml-pred #(.equals \/ %))),,,,,,, #_" 29,568462 ns"
  (-> "///path" (triml-char \/)),,,,,,,,,,,,,,,,,,,, #_" 30,546831 ns"
  (-> "///path" (triml-pred #(= \/ %))),,,,,,,,,,,,, #_" 45,734948 ns (!)"
  (-> "///path" (triml-pred #(slash-char? %))),,,,,, #_" 28,151604 ns"
  (-> "///path" (triml-pred slash-char?)),,,,,,,,,,, #_" 29,075210 ns"
  (-> "///path" (triml-pred #(char-of? "/" %))),,,,, #_" 34,187005 ns"
  (-> "///path" (triml-pred (partial char-of? "/"))) #_" 65,913243 ns (!)"
  (-> "///path" (triml-chars "/")),,,,,,,,,,,,,,,,,, #_" 33,469147 ns")

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn only-pred?
  "True if `s` contains only chars by `pred`. False when `s` is empty."
  {:test (fn [] (t/are [expected actual] (= expected actual)
                  false (only-pred? char-digit? nil)
                  false (only-pred? char-digit? "")
                  false (only-pred? char-digit? "---")
                  true, (only-pred? char-digit? "123")))}
  [pred, ^CharSequence s]
  (if s (let [len (.length s)]
          (loop [index (int 0)]
            (if (= len index)
              (pos? len)
              (if (pred (.charAt s index))
                (recur (inc index))
                false))))
        false))

(defn only-chars-equal?
  "True if `s` contains only char `c`. False when `s` is empty."
  {:test (fn [] (t/are [expected actual] (= expected actual)
                  true, (only-chars-equal? \_ "___")
                  false (only-chars-equal? \_ "___x")))
   :inline (fn [c s] `(only-pred? (fn'char-equals? ~c) ~s))}
  [c, s]
  (only-pred? (fn'char-equals? c) s))

(defn only-chars-of?
  "True if `s` contains only chars of `cs`. False when `s` is empty."
  {:test (fn [] (t/are [expected actual] (= expected actual)
                  true, (only-chars-of? "_" "___")
                  false (only-chars-of? "_" "___x")))
   :inline (fn [cs s] `(only-pred? (fn'char-of? ~cs) ~s))}
  [cs, s]
  (only-pred? (fn'char-of? cs) s))

(defn digits?
  "True if `s` contains only digits."
  {:test   (fn [] (t/are [expected actual] (= expected actual)
                    true, (digits? "1234567890") #_"41 ns"
                    false (digits? "1234567890x")
                    false (digits? nil)
                    false (digits? "")))
   :inline (fn [s] `(only-pred? char-digit? ~s))}
  [s]
  (only-pred? char-digit? s))

(comment
  (->> "     " string/blank?),,,,,,,,,,,,,,,, #_" 19,034658 ns"
  (->> "     " cuerdas/blank?),,,,,,,,,,,,,,, #_"441,591121 ns (!)"
  (->> "     " (only-pred? char-whitespace?)) #_" 23,209841 ns"
  (->> "     " (only-chars-equal? \space)),,,,#_" 17,514059 ns"

  (->> "1234567890" cuerdas/digits?),,,,,,,,,,,,,, #_"183,252701 ns (!)"
  (->> "1234567890" digits?),,,,,,,,,,,,,,,,,,,,,, #_" 41,118658 ns"
  (->> "1234567890" (only-chars-of? "1234567890")) #_" 64,386593 ns"
  (->> "----------" digits?),,,,,,,,,,,,,,,,,,,,,, #_"  7,591519 ns")

