(defproject clojure-benchmarks "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [; clojure
                 [org.clojure/clojure "1.10.1"]
                 ; libs
                 [cljstache "2.0.1"]
                 [comb "0.1.1"]
                 [fleet "0.10.2"]
                 [hbs "1.0.2"]
                 [me.shenfeng/mustache "1.1"]
                 [mustache.clojure "0.3.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [stencil "0.5.0"]
                 [com.github.spullara.mustache.java/compiler "0.9.6"]
                 ; benchmark
                 [criterium "0.4.4"]])
