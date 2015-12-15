(ns reactrails-in-reagent.comment
  (:require
    [clojure.pprint :as pp]
    [liberator.core :refer [resource]]
    [datomic.api :as d]))


(defn make-comment [author text]
  {:comment/author author
   :comment/text text
   :comment/created (java.util.Date.)})


(def all-comments '[:find [(pull ?e [*]) ...]
                    :where
                    [?e :comment/text]])



(defn get-all-comments [conn]
  (d/q all-comments (d/db conn)))

(defn response-comment-list [ctx]
  (let [conn (get-in ctx [:request :conn])
        comments (get-all-comments conn)]
    (pr-str comments)))

(def comment-list (resource {:available-media-types ["text/plain"]
                             :handle-ok response-comment-list}))


(defn get-comment [id conn]
  (println (str "id requested " id))
  (d/pull (d/db conn) '[*] id))

;; TODO sanitaze params with coercion from prismatic schema

(defn response-comment-entry [ctx]
  (let [request (:request ctx)
        conn (:conn request)
        {id :id} (:route-params request)
        comment (get-comment (Long/parseLong id) conn)
        _ (println comment)]
    (pr-str comment)))

(def comment-entry (resource {:available-media-types ["text/plain"]
                              :handle-ok response-comment-entry}))