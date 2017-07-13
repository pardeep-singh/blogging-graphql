(ns blogging-graphql.middlewares
  (:require [clojure.tools.logging :as ctl]))

(defn wrap-exceptions
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (ctl/error e)
        {:error "Internal Server Error."}))))
