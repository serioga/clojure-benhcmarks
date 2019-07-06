(ns clojure-benchmarks.infix-to-prefix)

(set! *warn-on-reflection* true)


(defn convert
  [xs]
  (let [[x op1 & [y op2 z & r2 :as r1]] xs
        hi-op #{* '* / '/}]
    (cond
      (nil? x) xs

      (and (hi-op op1) z)
      (convert (concat (list (list op1 x y) op2 z) r2))

      :default
      (list op1 x (if op2 (convert r1) y)))))

(comment
  (#{* /} *)
  (convert '(1 + 2 + 3 + 4))
  (convert '(1 * 2 * 3 * 4))
  (convert '(1 + 2 * 3 - 4))
  (convert '(1 / 2 * 3 / 4))
  (convert '(1 * 2 - 3 * 4)))
