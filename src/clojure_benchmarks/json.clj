(ns clojure-benchmarks.json
  (:require [cheshire.core :as cheshire]
            [clojure.data.json :as data-json]
            [jsonista.core :as jsonista]))

(set! *warn-on-reflection* true)

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def test-string "{\"a\":1,\"b\":2}")
(def test-data {:a 1 :b 2})

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(comment
  "Parse JSON string."

  (cheshire/parse-string test-string)                       ; Execution time mean : 1,335766 µs
  (cheshire/parse-string-strict test-string)                ; Execution time mean : 1,587872 µs
  (cheshire/parse-string test-string true)                  ; Execution time mean : 1,733945 µs
  (cheshire/parse-string-strict test-string true)           ; Execution time mean : 1,340044 µs

  (data-json/read-str test-string)                          ; Execution time mean : 845,653038 ns
  (data-json/read-str test-string :key-fn keyword)          ; Execution time mean : 1,336589 µs

  (jsonista/read-value test-string)                         ; Execution time mean : 473,904146 ns
  (jsonista/read-value test-string
                       jsonista/keyword-keys-object-mapper) ; Execution time mean : 522,288559 ns
  )

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(comment
  "Generate JSON string."

  (cheshire/generate-string test-data)                      ; Execution time mean : 1,511262 µs

  (data-json/write-str test-data)                           ; Execution time mean : 761,501998 ns

  (jsonista/write-value-as-string test-data)                ; Execution time mean : 275,184350 ns
  )

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
