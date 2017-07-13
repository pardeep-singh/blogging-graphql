(ns blogging-graphql.core
  (:require [compojure.core :as cc :refer [context defroutes POST GET]]
            [compojure.route :as route]
            [cheshire.core :as json]
            [clojure.tools.logging :as ctl]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware
             [keyword-params :refer [wrap-keyword-params]]
             [multipart-params :refer [wrap-multipart-params]]
             [params :refer [wrap-params]]]
            [blogging-graphql.middlewares :as bgm]
            [ring.util.response :as response]
            [blogging-graphql.schema :as bgs]
            [clojure.string :as str]
            [com.walmartlabs.lacinia :refer [execute]]))


(defonce server nil)
(def compiled-schema (bgs/compiled-blogs-schema))


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
      (json/parse-string vars true)
      {})))


(defn post-req
  [request]
  (let [query (extract-query request)
        vars (variable-map request)
        result (execute compiled-schema query vars nil)
        status (if (-> result :errors seq)
                 400
                 200)]
    {:status status
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string result)}))


(defn get-req
  [request]
  (let [query (extract-query request)
        vars (variable-map request)
        result (execute compiled-schema query nil)
        status (if (-> result :errors seq)
                 400
                 200)]
    {:status status
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string result)}))


(defn api-routes
  []
  (cc/defroutes routes

    (GET "/ping" [] "PONG")

    (GET "/" []
         (response/resource-response "index.html" {:root "public"}))

    (GET "/graphql" req
         (get-req req))

    (POST "/graphql" req
          (post-req req))

    (route/resources "/")
    (route/not-found "Not Found")))


(defn app
  []
  (-> (api-routes)
      wrap-keyword-params
      wrap-params
      bgm/wrap-exceptions))


(defn start-server
  [port]
  (let [server (run-jetty (app)
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
