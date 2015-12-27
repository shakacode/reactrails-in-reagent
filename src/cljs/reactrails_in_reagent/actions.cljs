(ns reactrails-in-reagent.actions
  (:require
    [reactrails-in-reagent.dispatch :as d]
    [reactrails-in-reagent.client :as client]))


(defrecord ReceivedComment [comment]
  d/Message
  (process-message* [this app]
    (let [c (:comment this)]
      (update app :comments conj c))))


(defrecord NewComment [comment]
  d/EventSource
  (watch-channels [this]
    (let [[channel-res cb] (client/result-in-channel ->ReceivedComment)]
      (client/send-new-comment! (:comment this) cb)
      #{channel-res})))


(defrecord ReceivedAllComments [comments]
  d/Message
  (process-message* [this app]
    (let [current-comments (:comments app)
          with-new-ones (reduce conj current-comments (:comments this))]
      (assoc app :comments with-new-ones))))


(defrecord GetAllComments []
  d/EventSource
  (watch-channels [_]
    (let [[channel-res cb] (client/result-in-channel ->ReceivedAllComments)]
      (client/get-all-comments! cb)
      #{channel-res})))


(defrecord SelectFormStyle [style]
  d/Message
  (process-message* [this app]
    (assoc app :nav/index (:style this))))
