(ns blogging-graphql.db
  (:require [clojure.set :refer [rename-keys]]))


(def data (atom {:authors-id-counter 3
                 :comments-id-counter 5
                 :blogs-id-counter 3
                 :authors {1 {:id 1
                              :name "Default Author"
                              :email "default@mail.com"}
                           2 {:id 2
                              :name "Author2"
                              :email "author2@mail.com"}}
                 :blogs {1 {:id 1
                            :body "First blog"
                            :author-id 1}
                         2 {:id 2
                            :body "Second blog"
                            :author-id 2}}
                 :comments {1 {:id 1
                               :body "First Comment"
                               :author-id 1
                               :blog-id 1}
                            2 {:id 2
                               :body "Second Comment"
                               :author-id 2
                               :blog-id 1}
                            3 {:id 3
                               :body "Third Comment"
                               :author-id 2
                               :blog-id 2}
                            4 {:id 4
                               :body "Second Comment"
                               :author-id 2
                               :blog-id 2}}}))


(defn create-author
  "Creates an author given a name and email."
  [ctx args values]
  (let [author-id (:author-id-counter @data)
        author-record (assoc args
                             :id author-id)]
    (swap! data update :author-id-counter inc)
    (swap! data assoc-in [:authors author-id] author-record)
    author-record))


(defn get-author
  "Returns an author given an author-id."
  [ctx args values]
  (if (:id args)
    (get-in @data [:authors (:id args)])
    (get-in @data [:authors (:author-id values)])))


(defn get-all-authors
  "Returns all authors. May support pagination in future."
  [ctx args values]
  (vals (get-in @data [:authors])))


(defn get-blog
  "Returns a blog given the blog id."
  [ctx args values]
  (get-in @data [:blogs (:id args)]))


(defn get-all-blogs
  "Retruns all the blogs"
  [ctx args values]
  (let [blogs (vals (get-in @data [:blogs]))
        blogs-paylod {:totalBlogs (count blogs)}]
    (if (> (:skip args)
           0)
      (assoc blogs-paylod
             :nodes (take (:last args)
                          (drop (:skip args)
                                blogs)))
      (assoc blogs-paylod
             :nodes (take (:first args)
                          blogs)))))


(defn get-comments
  "Returns all the comments for a given blog."
  [ctx args values]
  (let [blog-id (if (:blogID args)
                  (:blogID args)
                  (:id values))
        comments (filterv #(= blog-id
                              (:blog-id %))
                          (vals (get-in @data [:comments])))
        comments-payload {:totalComments (count comments)}]
    (if (> (:skip args)
           0)
      (assoc comments-payload
             :nodes (take (:last args)
                          (drop (:skip args)
                                comments)))
      (assoc comments-payload
             :nodes (take (:first args)
                          comments)))))


(defn post-comment
  "Post a comment to the blog."
  [ctx args values]
  (let [comment-id (:comments-id-counter @data)
        comment-data (-> args
                         (assoc :id comment-id)
                         (rename-keys {:blogID :blog-id
                                       :authorID :author-id}))]
    (swap! data update :comments-id-counter inc)
    (swap! data assoc-in [:comments comment-id] comment-data)
    comment-data))


(defn post-blog
  "Post a blog."
  [ctx args values]
  (let [blog-id (:blogs-id-counter @data)
        blog-data (-> args
                      (assoc :id blog-id)
                      (rename-keys {:authorID :author-id}))]
    (swap! data update :blogs-id-counter inc)
    (swap! data assoc-in [:blogs blog-id] blog-data)
    blog-data))
