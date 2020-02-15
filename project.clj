(defproject clojure-benchmarks "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [; clojure
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.7.559"]
                 [org.clojure/core.match "0.3.0"]
                 ; http router
                 [functionalbytes/sibiro "0.1.5"]
                 [metosin/reitit "0.4.2"]
                 ; transducers
                 [net.cgrand/xforms "0.19.2"]
                 ; templating
                 [cljstache "2.0.5"]
                 [com.github.spullara.mustache.java/compiler "0.9.6"]
                 [comb "0.1.1"]
                 [fleet "0.10.2"]
                 [hbs "1.0.3"]
                 [me.shenfeng/mustache "1.1"]
                 [mustache.clojure "0.3.0"]
                 [stencil "0.5.0"]
                 ; benchmark
                 [criterium "0.4.5"]])
