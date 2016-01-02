(ns reactrails-in-reagent.handler.middleware
  (:require [clojure.pprint :as pp]))


(defn wrap-assoc-request [handler & kvs]
  (fn [request]
    (handler (apply assoc request kvs))))

(defn wrap-dump-req [handler]
  (fn [request]
    (pp/pprint request)
    (handler request)))