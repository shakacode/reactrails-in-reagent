(ns reactrails-in-reagent.api
  (:require [ajax.core :refer [GET POST PUT]]))

(defn error-handler [{:keys [status failure response]}]
  (js/alert (str failure " " response)))

(defn api [method path params ok & [err]]
  (method (str "/api" path) {:params params
                              :handler ok
                              :format :transit
                              :response-format :transit
                              :error-handler (or err error-handler)}))

(defn get-comments [ok & [err]]
  (api GET "/comments" nil ok err))

(defn save-comment [comment ok & [err]]
  (api PUT "/comments" comment ok err))
