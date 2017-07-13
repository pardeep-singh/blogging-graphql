(ns blogging-graphql.db)


(def data (atom {:author_id_counter 3
                 :authors {1 {:id 1
                              :name "Default Author"
                              :email "default@mail.com"}
                           2 {:id 2
                              :name "Author2"
                              :email "author2@mail.com"}}
                 :blogs {1 {:id 1
                            :body "First blog"
                            :author_id 1}
                         2 {:id 2
                            :body "Second blog"
                            :author_id 2}}
                 :comments {1 {:id 1
                               :body "First Comment"
                               :author_id 1
                               :blog_id 1}
                            2 {:id 2
                               :body "Second Comment"
                               :author_id 2
                               :blog_id 1}
                            3 {:id 3
                               :body "Third Comment"
                               :author_id 2
                               :blog_id 2}
                            4 {:id 4
                               :body "Second Comment"
                               :author_id 2
                               :blog_id 2}}}))


(defn create-author
  "Creates an author given a name and email."
  [ctx args values]
  (let [author_id (:author_id_counter @data)
        author-record (assoc args
                             :id author_id)]
    (swap! data update :author_id_counter inc)
    (swap! data assoc-in [:authors author_id] author-record)
    author-record))


(defn get-author
  "Returns an author given an author-id."
  [ctx args values]
  (if (:id args)
    (get-in @data [:authors (:id args)])
    (get-in @data [:authors (:author_id values)])))


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
  (vals (get-in @data [:blogs])))


(defn get-comments
  "Returns all the comments for a given blog."
  [ctx args values]
  (let [blog_id (if (:blog_id args)
                  (:blog_id args)
                  (:id values))]
    (filterv #(= blog_id
                 (:blog_id %))
             (vals (get-in @data [:comments])))))
