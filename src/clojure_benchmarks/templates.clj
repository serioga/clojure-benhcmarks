(ns clojure-benchmarks.templates
  (:require
    [cljstache.core :as cljstache]
    [comb.template :as comb]
    [criterium.core :as criterium]
    [fleet :as fleet]
    [hbs.core :as hbs]
    [me.shenfeng.mustache :as mustache]
    [mustache.core :as mustache-java]
    [stencil.core :as stencil]
    [stencil.parser :as stencil-parser]))

(set! *warn-on-reflection* true)


(def template-params
  {:code 9999
   :message "error message"})


(defn str-template
  [{:keys [code message]}]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns2:error_response xmlns:ns2='http://api.forticom.com/1.0/'>\n<error_code>"
       code
       "</error_code>\n<error_msg>"
       message
       "</error_msg>\n</ns2:error_response>"))


(def fleet-template (fleet/fleet [params] "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns2:error_response xmlns:ns2='http://api.forticom.com/1.0/'>\n<error_code><(:code params)></error_code>\n<error_msg><(:message params)></error_msg>\n</ns2:error_response>"))


(def comb-fn-template
  (comb/fn [params] "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns2:error_response xmlns:ns2='http://api.forticom.com/1.0/'>\n<error_code><%= (:code params) %></error_code>\n<error_msg><%= (:message params) %></error_msg>\n</ns2:error_response>"))


(def stencil-parsed
  (stencil-parser/parse "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns2:error_response xmlns:ns2='http://api.forticom.com/1.0/'>\n<error_code>{{code}}</error_code>\n<error_msg>{{message}}</error_msg>\n</ns2:error_response>"))


(defn stencil-template
  [data]
  (stencil/render stencil-parsed data))


(def mustache-java-template
  (mustache-java/mustache-compile (mustache-java/mustache-factory)
                                  "mustache-java.mustache"))

(mustache/deftemplate
  mustache-template
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns2:error_response xmlns:ns2='http://api.forticom.com/1.0/'>\n<error_code>{{code}}</error_code>\n<error_msg>{{message}}</error_msg>\n</ns2:error_response>")


(defn cljstache-template
  [data]
  (cljstache/render "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ns2:error_response xmlns:ns2='http://api.forticom.com/1.0/'>\n<error_code>{{code}}</error_code>\n<error_msg>{{message}}</error_msg>\n</ns2:error_response>"
                    data))


(comment
  (criterium/quick-bench
    ; Execution time mean : 121,053870 µs
    (cljstache-template template-params))

  (criterium/quick-bench
    ; Execution time mean : 475,070705 ns
    (mustache-java-template template-params))

  (criterium/quick-bench
    ; Execution time mean : 612,872623 ns
    (mustache-template template-params))

  (criterium/quick-bench
    ; Execution time mean : 1,237585 µs
    (stencil-template template-params))

  (criterium/quick-bench
    ; Execution time mean : 420,090694 ns
    (str-template template-params))

  (criterium/quick-bench
    ; Execution time mean : 18,867465 µs
    (fleet-template template-params))

  (criterium/quick-bench
    ; Execution time mean : 4,153316 µs
    (comb-fn-template template-params)))
