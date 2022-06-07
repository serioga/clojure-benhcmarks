(ns clojure-benchmarks.json
  (:require [cheshire.core :as cheshire]
            [clojure.data.json :as data-json]
            [jsonista.core :as jsonista]
            [pjson.core :as pjson]))

(set! *warn-on-reflection* true)

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def test-string "{\"a\":1,\"b\":2}")
(def test-data {:a 1 :b 2})

(comment
  (def test-string "{\"multipleBirthBoolean\":true,\"address\":[{\"use\":\"home\",\"line\":[\"Van Egmondkade 23\"],\"city\":\"Amsterdam\",\"postalCode\":\"1024 RJ\",\"country\":\"NLD\"}],\"managingOrganization\":{\"display\":\"Burgers University Medical Centre\"},\"deceasedBoolean\":false,\"name\":[{\"use\":\"usual\",\"family\":\"van de Heuvel\",\"given\":[\"Pieter\"],\"suffix\":[\"MSc\"]}],\"birthDate\":\"1944-11-17\",\"resourceType\":\"Patient\",\"active\":true,\"communication\":[{\"language\":{\"coding\":[{\"system\":\"urn:ietf:bcp:47\",\"code\":\"nl\",\"display\":\"Dutch\"}],\"text\":\"Nederlands\"},\"preferred\":true}],\"identifier\":[{\"use\":\"usual\",\"system\":\"urn:oid:2.16.840.1.113883.2.4.6.3\",\"value\":\"738472983\"},{\"use\":\"usual\",\"system\":\"urn:oid:2.16.840.1.113883.2.4.6.3\"}],\"telecom\":[{\"system\":\"phone\",\"value\":\"0648352638\",\"use\":\"mobile\"},{\"system\":\"email\",\"value\":\"p.heuvel@gmail.com\",\"use\":\"home\"}],\"gender\":\"male\",\"maritalStatus\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v3-MaritalStatus\",\"code\":\"M\",\"display\":\"Married\"}],\"text\":\"Getrouwd\"},\"contact\":[{\"relationship\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0131\",\"code\":\"C\"}]}],\"name\":{\"use\":\"usual\",\"family\":\"Abels\",\"given\":[\"Sarah\"]},\"telecom\":[{\"system\":\"phone\",\"value\":\"0690383372\",\"use\":\"mobile\"}]}]}")
  (def test-data (get-in (pjson/read-str test-string) ["name" 0 "family"])))

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(comment
  "Parse JSON string."

  (cheshire/parse-string test-string)             ; Execution time mean : 1,335766 µs
  (cheshire/parse-string-strict test-string)      ; Execution time mean : 1,587872 µs
  (cheshire/parse-string test-string true)        ; Execution time mean : 1,733945 µs
  (cheshire/parse-string-strict test-string true) ; Execution time mean : 1,340044 µs

  (data-json/read-str test-string)                ; Execution time mean : 845,653038 ns
  (data-json/read-str test-string :key-fn keyword) ; Execution time mean : 1,336589 µs

  (jsonista/read-value test-string)               ; Execution time mean : 473,904146 ns
  (jsonista/read-value test-string
                       jsonista/keyword-keys-object-mapper) ; Execution time mean : 522,288559 ns

  (pjson/read-str test-string)                    ; Execution time mean : 170,064847 ns

  (->> (pjson/read-str test-string)
       (clojure.walk/prewalk identity))
  (->> (jsonista/read-value test-string)
       (clojure.walk/prewalk identity))

  )

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(comment
  "Generate JSON string."

  (cheshire/generate-string test-data)            ; Execution time mean : 1,511262 µs

  (data-json/write-str test-data)                 ; Execution time mean : 761,501998 ns

  (jsonista/write-value-as-string test-data)      ; Execution time mean : 275,184350 ns

  (pjson/generate-string test-data)               ; Execution time mean : 250,452683 ns
  )

;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
