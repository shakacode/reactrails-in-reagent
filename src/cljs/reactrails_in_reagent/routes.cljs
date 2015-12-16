(ns reactrails-in-reagent.routes
    (:require [secretary.core :as secretary :refer-macros [defroute]]
              [clojure.string :as str]
              [re-frame.core :as rf]
              [reactrails-in-reagent.util :as util]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.history.Html5History
             goog.Uri))


(def history (Html5History.))

;; ----------
;; History
(defn hook-browser-navigation! [prefix]
  (doto history
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (let [t (.-token event)
             rt (if (str/blank? t) "/" t)]
;;          (println :dispatch rt)
         (secretary/dispatch! rt))))
    (.setUseFragment false)
    (.setPathPrefix prefix)
    (.setEnabled true)))

;; ----------
;; Routes
(defn app-routes [prefix]
  (secretary/set-config! :prefix prefix)

  (defroute non-router-comment-screen "/" {:as params} (rf/dispatch [:set-active-page :non-router-comment-screen params]))

  (hook-browser-navigation! prefix)

  (events/listen js/document "click"
     (fn [e]
       (let [a ((fn [e]
                 (if-let [href (.-href e)]
                   e
                   (when-let [parent (.-parentNode e)]
                     (recur parent)))) (.-target e))]
         (when a
           (let [href (.-href a)
                 path (.getPath (.parse Uri href))
                 title (or (.-title a) "")]
             (when (and (> (count path) 0) (util/starts-with path prefix))
               (. history (setToken (subs path (count prefix)) title)))
             (.preventDefault e)))))))
