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


;; TODO rework to handle concurency cases
(defrecord ReceivedAllComments [comments]
  d/Message
  (process-message* [this app]
    (assoc app :comments (:comments this))))


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
