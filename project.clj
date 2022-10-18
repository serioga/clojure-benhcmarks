(defproject clojure-benchmarks "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [; clojure
                 [org.clojure/clojure "1.11.0"]
                 [org.clojure/core.async "1.2.603"]
                 [org.clojure/core.match "1.0.0"]
                 [medley "1.3.0"]
                 [potemkin "0.4.5"]
                 ; http router
                 [functionalbytes/sibiro "0.1.5"]
                 [metosin/reitit "0.5.2"]
                 ; json
                 [cheshire "5.10.2"]
                 [com.fasterxml.jackson.core/jackson-core "2.13.2"]
                 [metosin/jsonista "0.3.5"]
                 [org.clojure/data.json "2.4.0"]
                 [pjson "0.5.2"]
                 ; string
                 [funcool/cuerdas "2020.03.26-3"]
                 [superstring "3.0.0"]
                 ; transducers
                 [net.cgrand/xforms "0.19.2"]
                 ; templating
                 [cljstache "2.0.6"]
                 [com.github.spullara.mustache.java/compiler "0.9.10"]
                 [com.samskivert/jmustache "1.15"]
                 [comb "0.1.1"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [fleet "0.10.2"]
                 [hbs "1.0.3"]
                 [malabarba/lazy-map "1.3"]
                 [me.shenfeng/mustache "1.1"]
                 [mustache.clojure "0.3.0"]
                 [org.panchromatic/mokuhan "0.1.1"]
                 [pogonos "0.2.0"]
                 [stencil "0.5.0"]

                 ; benchmark
                 [criterium "0.4.6"]])
