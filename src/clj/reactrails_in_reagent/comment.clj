(ns reactrails-in-reagent.comment
  (:require
    [clojure.pprint :as pp]
    [schema.core :as s]
    [schema.coerce :as coerce]
    [schema.utils :as s-utils]
    [liberator.core :refer [resource]]
    [datomic.api :as d]
    [bidi.bidi :as bidi]
    [reactrails-in-reagent.comment.schemas :refer [New-comment]]
    [reactrails-in-reagent.handler.utils :as h-utils]
    [reactrails-in-reagent.handler.middleware :refer [wrap-assoc-request]]
    [com.rpl.specter :as specter]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
    [clojure.pprint :as pp]
    ))

;; ----------------------------------------------------------------------------
;; generic comment handling

(def all-comments '[:find [(pull ?e [*]) ...]
                    :where
                    [?e :comment/text]])

(defn get-comment [id conn]
  (d/pull (d/db conn) '[*] id))

(defn get-all-comments [conn]
  (d/q all-comments (d/db conn)))

(defn transact-new-comment [conn comment]
  (let [t-id (d/tempid :db.part/user)
        comment (assoc comment
                  :db/id t-id
                  :comment/created (java.util.Date.))
        result @(d/transact conn [comment])
        {:keys [db-after tempids]} result]
    (d/resolve-tempid db-after tempids t-id)))


;; ----------------------------------------------------------------------------
;; Resources definition


(defn- proper-keys [c]
  (clojure.set/rename-keys c {"author" :comment/author
                              "text" :comment/text}))

(def comment-params-coercer
  (coerce/coercer
    New-comment
    (fn [s]
      (if (= s New-comment)
        #(proper-keys %)))))

(defn malformed-comment-list-params? [ctx]
  (let [request (:request ctx)
        method (:request-method request)]
    (case method
      :post (let [comment (-> request :params comment-params-coercer)]
              (if-not (s-utils/error? comment)
                [false {::comment comment}]
                true))
      :get false)))

(defn response-comment-list [ctx]
  (let [conn (get-in ctx [:request :conn])
        comments (get-all-comments conn)]
    comments))

(defn post-comment! [ctx]
  (let [comment (::comment ctx)
        conn (-> ctx :request :conn)]
    {::id (transact-new-comment conn comment)}))

(defn post-redirection [ctx]
  (let [id (::id ctx)
        routes (-> ctx :request :routes)]
    {:location (bidi/path-for routes :comments/comment-entry :id id)}))

(def comment-list
  (resource {:available-media-types ["application/json"]
             :allowed-methods [:post :get]
             :malformed? malformed-comment-list-params?
             :handle-ok response-comment-list
             :post! post-comment!
             :post-redirect? post-redirection}))



(s/defschema comment-entry-params-schema {(s/required-key :id) Long})

(def comment-entry-params-coercer
  (coerce/coercer comment-entry-params-schema
                  {Long (fn [possible-long]
                          ((coerce/safe #(Long/parseLong %)) possible-long))}))

(defn malformed-comment-entry-params? [ctx]
  (let [request (:request ctx)
        params (:route-params request)
        checked-params (comment-entry-params-coercer params)]
    (if-not (s-utils/error? checked-params)
      [false {:checked-params checked-params}]
      true)))

(defn response-comment-entry [ctx]
  (let [conn (get-in  ctx [:request :conn])
        {id :id} (:checked-params ctx)
        comment (get-comment id conn)]
    comment))


(def comment-entry
  (resource {:available-media-types ["application/json"]
             :malformed?            malformed-comment-entry-params?
             :handle-ok             response-comment-entry}))


;; ----------------------------------------------------------------------------
;; Routes definition


(h-utils/register-handler! 'comments/comment-list comment-list)
(h-utils/register-handler! 'comments/comment-entry comment-entry)


(defn make-transformations [handler-component]
  {(specter/multi-path 'comments/comment-list
                       'comments/comment-entry)
   #(-> %
        (wrap-assoc-request :conn (-> handler-component :database :connection)
                                    :routes (:routes-definition handler-component))
        (wrap-params)
        (wrap-json-params)
        )})
