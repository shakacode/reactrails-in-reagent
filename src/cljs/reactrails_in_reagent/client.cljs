(ns reactrails-in-reagent.client
  (:require
    [schema.core :as s :include-macros true]
    [schema.coerce :as coerce]
    [bidi.bidi :as bidi]
    [reactrails-in-reagent.routes :refer [routes]]
    [reactrails-in-reagent.comment.schemas :refer [New-comment]]
    [cljs.core.async :as async]
    [clojure.set])
  (:import
    [goog.net XhrIo]))

(enable-console-print!)

(defn clj->JSON [v]
  (->> v
       clj->js
       (.stringify js/JSON )))

(defn JSON->clj [v]
  (let [v' (.parse js/JSON v)
        _ (println "parsed" v')]
    (->> v
         (.parse js/JSON)
         (js->clj))))

(s/defschema Comment (merge New-comment
                            {(s/required-key :comment/created) js/Date
                             (s/required-key :db/id) js/Number}))


(defn replace-with-peoper-keys [m]
  (clojure.set/rename-keys m {"comment/author"   :comment/author
                              "comment/text"    :comment/text
                              "comment/created" :comment/created
                              "db/id"      :db/id}))

(def comment-coercer
  (coerce/coercer Comment
                  {Comment replace-with-peoper-keys
                   js/Date #(js/Date. %)
                   }))



(def comment-list-route (bidi/path-for routes 'comments/comment-list))

(defn send-new-comment! [c cb]
  (.send XhrIo comment-list-route
         (fn [_]
           (this-as this
             (let [r (.getResponseText this)
                   clj-r (JSON->clj r)
                   coerced (comment-coercer clj-r)]
               (println coerced)
               (cb coerced))))
         "POST"
         (clj->JSON c)
         #js {"Content-Type" "application/json"}))


(defn get-all-comments! [cb]
  (.send XhrIo comment-list-route
         (fn [_]
           (this-as this
             (let [r (.getResponseText this)
                   clj-r (JSON->clj r)
                   coerced (map comment-coercer clj-r)]
               (cb coerced))))))


(defn result-in-channel [f]
  (let [channel-res (async/chan)]
    [channel-res (fn [res]
                   (async/put! channel-res (f res))
                   (async/close! channel-res))]))



