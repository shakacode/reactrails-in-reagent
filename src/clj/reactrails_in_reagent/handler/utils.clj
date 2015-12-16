(ns reactrails-in-reagent.handler.utils
  (:require
    [clojure.pprint :as pp]))


(def ^:private handlers (atom {}))

(defn register-handler!* [key h]
  {:pre [(var? h)]}
  (swap! handlers assoc key h))

(defmacro register-handler! [key h]
  {:pre [(symbol? h)]}
  (let [v (resolve h)]
    `(register-handler!* ~key ~v)))

(defn get-handlers-dev [] @handlers)

(defn deref-handlers [hs]
  (reduce-kv (fn [acc k v] (assoc acc k (deref v))) {} hs))

(defn get-handlers-prod []
  (deref-handlers @handlers))

