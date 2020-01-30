(ns clojure-benchmarks.http-router
  (:require
    [criterium.core :as criterium]
    [reitit.core :as reitit]
    [sibiro.core :as sibiro]))

(set! *warn-on-reflection* true)


(def sibiro-routes
  (sibiro/compile-routes
    [[:get "/api/1" nil]
     [:get "/api/2" nil]
     [:get "/api/3" nil]
     [:get "/api/4" nil]
     [:get "/api/5" nil]
     [:get "/api/ping" :route/ping]
     [:get "/api/orders/:id" :route/order-get]
     [:post "/api/orders/:id" :route/order-update]]))


(def reitit-router
  (reitit/router
    [["/api/1" {}]
     ["/api/2" {}]
     ["/api/3" {}]
     ["/api/4" {}]
     ["/api/5" {}]
     ["/api/ping" {:name :route/ping
                   :get :route/ping}]
     ["/api/orders/:id" {:name :route/order
                         :get :route/order-get
                         :post :route/order-update}]]))


(defn sibiro-match-path
  [method path]
  (sibiro/match-uri sibiro-routes path method))


(defn reitit-match-path
  [method path]
  (-> (reitit/match-by-path reitit-router path), :data, method))


(comment #_"Path matching"

  ; sibiro

  (criterium/quick-bench
    (sibiro-match-path :get "/api/ping")
    #_{:route-handler :route/ping, :route-params {}, :alternatives ()})
  #_"Execution time mean : 2,162359 µs"

  (criterium/quick-bench
    (sibiro-match-path :post "/api/orders/42")
    #_{:route-handler :route/order-update, :route-params {:id "42"}, :alternatives ()})
  #_"Execution time mean : 3,171040 µs"

  ; reitit

  (criterium/quick-bench
    (reitit-match-path :get "/api/ping")
    #_:route/ping)
  #_"Execution time mean : 31,768964 ns"

  (reitit/match-by-path reitit-router "/api/orders/42")
  #_#reitit.core.Match{:template "/api/orders/:id",
                       :data {:get :route/order-get, :post :route/order-update},
                       :result nil,
                       :path-params {:id "42"},
                       :path "/api/orders/42"}

  (criterium/quick-bench
    (reitit-match-path :get "/api/orders/42")
    #_:route/order)
  #_"Execution time mean : 91,298972 ns"

  (criterium/quick-bench
    (reitit-match-path :post "/api/orders/42")
    #_:route/order-update)
  #_"Execution time mean : 92,187590 ns"


  #_'comment #_"Path matching")


(defn sibiro-uri-for
  [tag params]
  (sibiro/uri-for sibiro-routes tag params))


(defn reitit-match-name
  [name params]
  (reitit/match-by-name reitit-router name params))


(comment #_"URI generation"

  ; sibiro

  (criterium/quick-bench
    (sibiro-uri-for :route/ping {})
    #_{:uri "/api/ping", :query-string nil})
  #_"Execution time mean : 533,789893 ns"

  (criterium/quick-bench
    (sibiro-uri-for :route/ping {:from "test"})
    #_{:uri "/api/ping", :query-string "?from=test"})
  #_"Execution time mean : 2,476628 µs"

  (criterium/quick-bench
    (sibiro-uri-for :route/order-update {:id "42" :name "test"})
    #_{:uri "/api/orders/42", :query-string "?name=test"})
  #_"Execution time mean : 3,065419 µs"

  ; reitit

  (reitit-match-name :route/ping {})

  (criterium/quick-bench
    (reitit-match-name :route/order {:id "42" :name "test"}))

  (criterium/quick-bench
    (-> (reitit-match-name :route/order {:id "42" :name "test"})
      (reitit/match->path {:id "42" :name "test"})))

  (reitit/routes reitit-router)


  #_'comment #_"URI generation")
