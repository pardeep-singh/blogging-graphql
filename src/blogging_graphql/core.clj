(ns blogging-graphql.core
  (:require [compojure.core :refer [defroutes POST GET]]
            [compojure.route :as route]
            [cheshire.core :as cc]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware
             [keyword-params :refer [wrap-keyword-params]]
             [params :refer [wrap-params]]]
            [blogging-graphql.middlewares :as bgm]
            [ring.util.response :as response]
            [blogging-graphql.schema :as bgs]
            [clojure.string :as str]
            [com.walmartlabs.lacinia :refer [execute]]
            [blogging-graphql.db :as bgd]))


(defonce server nil)


(defn extract-query
  [request]
  (case (:request-method request)
    :get (get-in request [:query-params "query"])
    :post (slurp (:body request))
    :else ""))


(defn variable-map
  [request]
  (let [vars (get-in request [:query-params "variables"])]
    (if-not (str/blank? vars)
      (cc/parse-string vars true)
      {})))


(defn handle-req
  [request compiled-schema]
  (let [query (extract-query request)
        vars (variable-map request)
        result (execute compiled-schema query vars nil)
        status (if (-> result :errors seq)
                 400
                 200)]
    {:status status
     :headers {"Content-Type" "application/json"}
     :body (cc/generate-string result)}))


(defn api-routes
  [compiled-schema]
  (defroutes routes

    (GET "/ping" [] "PONG")

    (GET "/" []
         (response/resource-response "index.html" {:root "public"}))

    (GET "/graphql" req
         (handle-req req compiled-schema))

    (POST "/graphql" req
          (handle-req req compiled-schema))

    (route/resources "/")

    (route/not-found "Not Found")))


(defn app
  [compiled-schema]
  (-> (api-routes compiled-schema)
      wrap-keyword-params
      wrap-params
      bgm/wrap-exceptions))


(defn start-server
  [port]
  (let [_ (bgd/generate-data)
        compiled-schema (bgs/compiled-blogs-schema)
        server (run-jetty (app compiled-schema)
                          {:port port
                           :join? false})]
    (alter-var-root #'server (constantly server))
    server))


(defn stop-server
  [server]
  (.stop server)
  (alter-var-root #'server (constantly nil)))


(defn -main
  [& args]
  (start-server 7000))
