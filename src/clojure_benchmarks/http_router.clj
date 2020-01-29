(ns clojure-benchmarks.http-router
  (:require
    [criterium.core :as criterium]
    [reitit.core :as reitit]
    [sibiro.core :as sibiro]))

(set! *warn-on-reflection* true)


(def sibiro-routes
  (sibiro/compile-routes
    #{[:get "/admin/user/" :route/user-list]
      [:get "/admin/user/:id" :route/user-get]
      [:post "/admin/user/:id" :route/user-update]}))


(defn sibiro-match-path
  [path method]
  (sibiro/match-uri sibiro-routes path method))


(def reitit-router
  (reitit/router
    [["/api/ping" {:get :route/ping}]
     ["/api/orders/:id" {:get :route/order}]]))


(defn reitit-match-path
  [path method]
  (-> (reitit/match-by-path reitit-router path), :data, method))


(comment
  (criterium/quick-bench
    (sibiro-match-path "/admin/user" :get)
    #_{:route-handler :route/user-list, :route-params {}, :alternatives ()})
  #_"Execution time mean : 2,056582 µs"

  (criterium/quick-bench
    (sibiro-match-path "/admin/user/42" :post)
    #_{:route-handler :route/user-update, :route-params {:id "42"}, :alternatives ()})
  #_"Execution time mean : 3,163475 µs"

  (criterium/quick-bench
    (reitit-match-path "/api/orders/42" :get)
    #_:route/order)
  #_"Execution time mean : 76,915492 ns"


  :comment)
