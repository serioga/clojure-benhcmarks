(ns clojure-benchmarks.potemkin
  (:require [potemkin]))

(set! *warn-on-reflection* true)

;;;•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

(potemkin/def-derived-map Remap [m]
  :x (:x m))

(def test-map {:x 1})
(def test-remap (->Remap {:x 1}))

(comment
  (:x test-map) #_"11 ns"
  (:x test-remap) #_"75 ns"
  (.valAt ^Remap test-remap :x) #_"71 ns"
  (.-m ^Remap test-remap)) #_"3 ns"
