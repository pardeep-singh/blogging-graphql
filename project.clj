(defproject blogging-graphql "0.1.0"
  :description "Sample Blogging APP using Graphql, Lacinia and Compojure."
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [cheshire "5.7.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [com.walmartlabs/lacinia "0.18.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
