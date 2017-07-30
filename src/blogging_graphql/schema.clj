(ns blogging-graphql.schema
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [blogging-graphql.db :as db]))


(defn compiled-blogs-schema
  "Returns a compiled blogs schema."
  []
  (-> (io/resource "blogs-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers {:getAuthor db/get-author
                              :createAuthor db/create-author
                              :getAllAuthors db/get-all-authors
                              :getBlog db/get-blog
                              :getBlogs db/get-all-blogs
                              :getComments db/get-comments
                              :postComment db/post-comment
                              :postBlog db/post-blog})
      schema/compile))
