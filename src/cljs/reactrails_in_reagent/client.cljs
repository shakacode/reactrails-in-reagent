(ns reactrails-in-reagent.client
  (:require
    [schema.core :as s :include-macros true]
    [schema.coerce :as coerce]
    [schema.utils :as s-utils]
    [reactrails-in-reagent.routes :as routes]
    [reactrails-in-reagent.comment.schemas :as schemas :refer [New-comment]]
    [cljs.core.async :as async]
    [clojure.set]
    [ajax.core :as ajax]
    [reactrails-in-reagent.routes :as routes])
  (:import
    [goog.net XhrIo]))

(enable-console-print!)

(defn default-error-handler [error]
  (println error))

(defn wrap-coercion
  "Wraps an ajax response handler so that the value passed to it is coerced
  with the given coercer. If the coercion fails calls the error handler instead."
  [coercer handler error-handler]
  (fn [response]
    (let [coerced (coercer response)]
      (if (s-utils/error? coerced)
        (error-handler {:type :coercion-error
                        :response response
                        :coerced coerced})
        (handler coerced)))))

(defn replace-with-peoper-keys [m]
  (clojure.set/rename-keys m {"comment/author"   :comment/author
                              "comment/text"    :comment/text
                              "comment/created" :comment/created
                              "db/id"      :db/id}))

(def comment-matcher
  {schemas/Comment replace-with-peoper-keys
   schemas/date-schema schemas/date-matcher})

(def comment-coercer (coerce/coercer schemas/Comment comment-matcher))
(def comment-list-coercer (coerce/coercer schemas/Comment-list comment-matcher))

(def comment-list-route (routes/path-for 'comments/comment-list))


(defn send-new-comment! [comment handler & [error-handler]]
  (println "sending with 2")
  (let [error-handler (or error-handler default-error-handler)]
    (ajax/POST comment-list-route
               {:format :json
                :params comment
                :handler (wrap-coercion comment-coercer handler error-handler)
                :error-handler error-handler})))



(defn get-all-comments! [handler & [error-handler]]
  (let [error-handler (or error-handler default-error-handler)]
    (ajax/GET comment-list-route
              {:handler (wrap-coercion comment-list-coercer handler error-handler)
               :error-handler error-handler})))


(defn make-core-async-callbacks
  "Given an ajax response handler and an optional error handler
  returns the tuple [async-chan handler' error-hendler'].

  async-chan will contain the value given by (handler ajax-response).

  The returned handlers are meant to be used with the ajax calls provided by this client
  and will close async-chan after putting a value on it or handling an error."
  [handler  & [error-handler]]
  (let [channel-res (async/chan)
        error-handler (or error-handler default-error-handler)

        handler'
        (fn [response]
          (async/put! channel-res (handler response))
          (async/close! channel-res))

        error-handler'
        (fn [error-response]
          (async/close! channel-res)
          (error-handler error-response))]
    [channel-res handler' error-handler']))

