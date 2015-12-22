(ns reactrails-in-reagent.client
  (:require
    [schema.core :as s :include-macros true]
    [schema.coerce :as coerce]
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




(defn send-new-comment! [c cb]
  (let [json-comment (clj->JSON c)]
    (.send XhrIo "/comments"
           (fn [_]
             (this-as this
               (let [r (.getResponseText this)
                     clj-r (JSON->clj r)
                     coerced (comment-coercer clj-r)]
                 (println coerced)
                 (cb coerced))))
           "POST"
           (clj->JSON c)
           #js {"Content-Type" "application/json"})))


(defn get-all-comments! [cb]
  (.send XhrIo "/comments"
         (fn [_]
           (this-as this
             (let [r (.getResponseText this)
                   clj-r (JSON->clj r)
                   coerced (map comment-coercer clj-r)]
               (cb coerced)))))
  (println "sent"))


(defn result-in-channel [f]
  (let [channel-res (async/chan)]
    [channel-res
     (fn [res]
       (-> channel-res
           (async/put! (f res))
           (async/close!)))]))
