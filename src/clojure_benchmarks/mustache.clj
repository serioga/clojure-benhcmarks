(ns clojure-benchmarks.mustache
  "Libs:
  - https://github.com/billrobertson42/mustache.clojure
  - https://github.com/davidsantiago/stencil
  - https://github.com/shenfeng/mustache.clj
  - https://github.com/fhd/clostache
  - https://github.com/athos/pogonos
  - https://github.com/ayato-p/mokuhan
  "
  (:require [clojure.java.io :as io]
            [clostache.parser :as clostache]
            [hbs.core :as hbs]
            [me.shenfeng.mustache :as mustache.shenfeng]
            [mustache.core :as mustache.clojure]
            [org.panchromatic.mokuhan.parser :as mokuhan.parser]
            [org.panchromatic.mokuhan.renderer :as mokuhan.renderer]
            [pogonos.core :as pogonos]
            [stencil.core :as stencil]
            [stencil.parser :as stencil-parser])
  (:import (clojure.lang Keyword)
           (com.github.jknack.handlebars Handlebars)
           (com.samskivert.mustache DefaultCollector Mustache Mustache$Collector Mustache$VariableFetcher)
           (java.util Map)))

(set! *warn-on-reflection* true)

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def template-name
  "clojure_benchmarks/mustache.mustache")

(def template-src
  (slurp (io/resource template-name)))

(def test-data
  {:user  {:name "Dolly"}
   :items [{:name "Item <1>" :price "$19.99" :features [{:description "New!"} {:description "Awesome!"}]}
           {:name "Item <2>" :price "$29.99" :features [{:description "Old."} {:description "Ugly."}]}]})

(def test-data-strs
  (clojure.walk/postwalk
    #(cond (keyword? %) (name %)
           :else %) test-data))

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def render-mustache-clojure
  (mustache.clojure/mustache-compile (mustache.clojure/mustache-factory) template-name))

(comment
  (render-mustache-clojure test-data)
  (print (render-mustache-clojure test-data))
  ;    Name: Item 1
  ;    Price: $19.99
  ;        Feature: New!
  ;        Feature: Awesome!
  ;    Name: Item 2
  ;    Price: $29.99
  ;        Feature: Old.
  ;        Feature: Ugly.
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def render-stencil
  (let [parsed (stencil-parser/parse template-src)]
    (fn [data]
      (stencil/render parsed data))))

(comment
  (render-stencil test-data)
  (print (render-stencil test-data))
  ;    Name: Item 1
  ;    Price: $19.99
  ;        Feature: New!
  ;        Feature: Awesome!
  ;    Name: Item 2
  ;    Price: $29.99
  ;        Feature: Old.
  ;        Feature: Ugly.
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(mustache.shenfeng/deftemplate render-shenfeng template-src)

(comment
  (render-shenfeng test-data)
  (print (render-shenfeng test-data))             ; (!) Extra lines
  ;
  ;    Name: Item 1
  ;    Price: $19.99
  ;
  ;        Feature: New!
  ;
  ;        Feature: Awesome!
  ;
  ;
  ;    Name: Item 2
  ;    Price: $29.99
  ;
  ;        Feature: Old.
  ;
  ;        Feature: Ugly.
  ;
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(defn render-clostache
  [data]
  (clostache/render template-src test-data))

(comment
  (render-clostache test-data)
  (print (render-clostache test-data))            ; (!) Escaped $
  ;    Name: Item 1
  ;    Price: \$19.99
  ;        Feature: New!
  ;        Feature: Awesome!
  ;    Name: Item 2
  ;    Price: \$29.99
  ;        Feature: Old.
  ;        Feature: Ugly.
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def render-pogonos
  (let [parsed (pogonos/parse-string template-src)]
    (fn [data]
      (pogonos/render parsed data))))

(comment
  (render-pogonos test-data)
  (print (render-pogonos test-data))
  ;    Name: Item 1
  ;    Price: $19.99
  ;        Feature: New!
  ;        Feature: Awesome!
  ;    Name: Item 2
  ;    Price: $29.99
  ;        Feature: Old.
  ;        Feature: Ugly.
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def render-mokuhan
  (let [parsed (mokuhan.parser/parse template-src)]
    (fn [data]
      (mokuhan.renderer/render parsed data))))

(comment
  (render-mokuhan test-data)
  (print (render-mokuhan test-data))
  ;    Name: Item 1
  ;    Price: $19.99
  ;        Feature: New!
  ;        Feature: Awesome!
  ;    Name: Item 2
  ;    Price: $29.99
  ;        Feature: Old.
  ;        Feature: Ugly.
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def render-hbs
  (let [reg (hbs/registry (hbs/classpath-loader "/clojure_benchmarks" ".hbs"))
        _ (-> reg (.getCache) (.setReload true))
        tpl (.compile ^Handlebars reg "mustache")]
    (fn [data]
      (.apply tpl data))))

(comment
  (render-hbs test-data)
  (def hbs-data (clojure.walk/postwalk
                  #(cond
                     #_#_(map? %) (java.util.HashMap. ^java.util.Map %)
                     (keyword? %) (name %)
                     :else %) test-data))
  (render-hbs hbs-data)
  (print (render-hbs hbs-data))
  (print (render-hbs test-data))
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(def jmustache-map-fetcher
  (reify Mustache$VariableFetcher
    (get [_ m n]
      (let [v (.get ^Map m (Keyword/intern ^String n))]
        (if (nil? v) (.get ^Map m n), v)))))

(defn jmustache-clojure-fetcher
  [^Mustache$VariableFetcher fetcher]
  (if (identical? "MAP_FETCHER" (some-> fetcher (.toString)))
    jmustache-map-fetcher
    fetcher))

(defn jmustache-collector
  []
  (let [collector (DefaultCollector.)]
    (reify Mustache$Collector
      (toIterator [_ value] (.toIterator collector value))
      (createFetcher [_ ctx name] (jmustache-clojure-fetcher (.createFetcher collector ctx name)))
      (createFetcherCache [_] (.createFetcherCache collector)))))

(def render-jmustache
  (let [c (-> (Mustache/compiler)
              (.withCollector (jmustache-collector))
              (.compile ^String template-src))]
    (fn [data]
      (.execute c data))))

(comment
  (identical? nil nil)
  (.containsKey ^Map {:x 1} :x)
  (render-jmustache test-data)
  (render-jmustache test-data-strs)
  (print (render-jmustache test-data-strs))
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(comment
  (render-mustache-clojure test-data)             ; Execution time mean :   1,639537 µs
  (render-stencil test-data)                      ; Execution time mean :   7,225870 µs
  (render-shenfeng test-data)                     ; Execution time mean :   1,610846 µs
  (render-clostache test-data)                    ; Execution time mean : 502,425587 µs
  (render-pogonos test-data)                      ; Execution time mean :   6,708580 µs
  (render-mokuhan test-data)                      ; Execution time mean : 994,134761 µs
  (render-jmustache test-data-strs)               ; Execution time mean :   1,966646 µs
  )

;;••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••
