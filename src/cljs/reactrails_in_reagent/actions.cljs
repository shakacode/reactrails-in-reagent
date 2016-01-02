(ns reactrails-in-reagent.actions
  (:require
    [reactrails-in-reagent.dispatch :as d]
    [reactrails-in-reagent.client :as client]))


(defrecord SelectFormStyle [style]
  d/Message
  (process-message* [this app]
    (assoc app :nav/index (:style this))))

(defrecord ReceivedComment [comment]
  d/Message
  (process-message* [this app]
    (let [c (:comment this)]
      (update app :comments conj c))))


(defrecord ReceivedAllComments [comments]
  d/Message
  (process-message* [this app]
    (let [current-comments (:comments app)
          with-new-ones (reduce conj current-comments (:comments this))]
      (assoc app :comments with-new-ones))))


(defrecord NewComment [comment]
  d/EventSource
  (watch-channels [this]
    (let [[channel-res handler error-handler] (client/make-core-async-callbacks ->ReceivedComment)]
      (client/send-new-comment! (:comment this) handler error-handler)
      #{channel-res})))


(defrecord GetAllComments []
  d/EventSource
  (watch-channels [_]
    (let [[channel-res handler error-handler] (client/make-core-async-callbacks ->ReceivedAllComments)]
      (client/get-all-comments! handler error-handler)
      #{channel-res})))



