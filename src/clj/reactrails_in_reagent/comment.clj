(ns reactrails-in-reagent.comment
  (:require
    [schema.core :as s]
    [schema.coerce :as coerce]
    [schema.utils :as s-utils]
    [liberator.core :refer [resource]]
    [datomic.api :as d]
    [reactrails-in-reagent.routes :as routes]
    [reactrails-in-reagent.comment.schemas :refer [New-comment long-schema long-coercion ]]
    [reactrails-in-reagent.handler.middleware :refer [wrap-assoc-request]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.json :refer [wrap-json-params]]))

;; ----------------------------------------------------------------------------
;; generic comment handling

(def all-comments '[:find [(pull ?e [*]) ...]
                    :where
                    [?e :comment/text]])

(defn get-comment [conn id]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?e
         :where [?e :comment/author]]
       (d/db conn)
       id))


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
  (coerce/coercer New-comment {New-comment #(proper-keys %)}))

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
  (let [conn (get-in ctx [:request :conn])]
    (get-all-comments conn)))

(defn post-comment! [ctx]
  (let [comment (::comment ctx)
        conn (-> ctx :request :conn)]
    {::id (transact-new-comment conn comment)}))

(defn post-redirection [ctx]
  (let [id (::id ctx)
        location (routes/path-for 'comments/comment-entry :id id)]
    {:location location}))

(def comment-list
  (resource {:available-media-types ["application/json"]
             :allowed-methods [:post :get]
             :malformed? malformed-comment-list-params?
             :handle-ok response-comment-list
             :post! post-comment!
             :post-redirect? post-redirection}))



(s/defschema comment-entry-params-schema {(s/required-key :id) long-schema})

(def comment-entry-params-coercer
  (coerce/coercer comment-entry-params-schema
                  {Long long-coercion}))

(defn malformed-comment-entry-params? [ctx]
  (let [request (:request ctx)
        params (:route-params request)
        checked-params (comment-entry-params-coercer params)]
    (if-not (s-utils/error? checked-params)
      [false {:checked-params checked-params}]
      true)))

(defn comment-entry-exists? [ctx]
  (let [conn (get-in  ctx [:request :conn])
        {id :id} (:checked-params ctx)
        comment (get-comment conn id)]
    (if comment
      [true {::comment comment}]
      false)))

(defn response-comment-entry [ctx]
  (::comment ctx))



(def comment-entry
  (resource {:available-media-types ["application/json"]
             :malformed?            malformed-comment-entry-params?
             :exists?               comment-entry-exists?
             :handle-ok             response-comment-entry}))


;; ----------------------------------------------------------------------------
;; Routes associations

(defn- middleware-list [handler-component]
  (comp wrap-json-params
        wrap-params
        #(wrap-assoc-request % :conn (-> handler-component :database :connection))))

(defn- middleware-entry [handler-component]
  (comp wrap-params
        #(wrap-assoc-request % :conn (-> handler-component :database :connection))))


(def end-points->handlers
  {'comments/comment-list comment-list
   'comments/comment-entry comment-entry})

(defn end-points->middlewares [handler-component]
  {'comments/comment-list  (middleware-list handler-component)
   'comments/comment-entry (middleware-entry handler-component)})
