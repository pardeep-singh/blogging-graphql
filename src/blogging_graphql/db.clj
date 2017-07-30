(ns blogging-graphql.db
  (:require [clojure.set :refer [rename-keys]])
  (:import java.util.UUID))


(def data (atom {}))


(defn create-author*
  [args]
  (let [author-id (str (UUID/randomUUID))
        author-data (assoc args
                           :id author-id)]
    (swap! data assoc-in [:authors author-id] author-data)
    author-data))


;; TODO: validate author-id and blog-id
(defn post-comment*
  [args]
  (let [comment-id (str (UUID/randomUUID))
        comment-data (-> args
                         (assoc :id comment-id)
                         (rename-keys {:blogID :blog-id
                                       :authorID :author-id}))]
    (swap! data assoc-in [:comments comment-id] comment-data)
    comment-data))


;; TODO: validate author-id
(defn post-blog*
  [args]
  (let [blog-id (str (UUID/randomUUID))
        blog-data (-> args
                      (assoc :id blog-id)
                      (rename-keys {:authorID :author-id}))]
    (swap! data assoc-in [:blogs blog-id] blog-data)
    blog-data))


(defn create-author
  "Creates an author given a name and email."
  [ctx args values]
  (create-author* args))


(defn post-comment
  "Post a comment to the blog."
  [ctx args values]
  (post-comment* args))


(defn post-blog
  "Post a blog."
  [ctx args values]
  (post-blog* args))


(defn get-author
  "Returns an author given an author-id."
  [ctx args values]
  (if (:id args)
    (get-in @data [:authors (:id args)])
    (get-in @data [:authors (:author-id values)])))


(defn get-all-authors
  "Returns all authors."
  [ctx args values]
  (vals (get-in @data [:authors])))


(defn get-blog
  "Returns a blog given the blog id."
  [ctx args values]
  (get-in @data [:blogs (:id args)]))


(defn get-all-blogs
  "Retruns all the blogs."
  [ctx args values]
  (let [blogs (vals (get-in @data [:blogs]))]
    (take (:first args)
          (drop (:skip args)
                blogs))))


(defn get-comments
  "Returns all the comments for a given blog."
  [ctx args values]
  (let [blog-id (if (:blogID args)
                  ;; when blogID is passed from getComments query.
                  (:blogID args)
                  ;; When nested is request inside blogs.
                  (:id values))
        comments (filterv #(= blog-id
                              (:blog-id %))
                          (vals (get-in @data [:comments])))]
    (take (:first args)
          (drop (:skip args)
                comments))))


(defn generate-data
  []
  (let [authors (doall (repeatedly 3 #(create-author* {:email "test@mail.com"
                                                       :name "test"})))
        blogs (doall (repeatedly 5 #(post-blog* {:authorID (:id (first authors))
                                                 :body "test"})))
        comments (doall (repeatedly 5 #(post-comment* {:authorID (:id (first authors))
                                                       :blogID (:id (first blogs))
                                                       :body "test"})))]))
