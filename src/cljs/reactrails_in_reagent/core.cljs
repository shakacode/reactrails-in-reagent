(ns reactrails-in-reagent.core
  (:require-macros [reactrails-in-reagent.cljsmacro :refer [with-subs defc handler-fn]])
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-frame.db]
            [clojure.string :as str]
            [reactrails-in-reagent.common :as c]
            [reactrails-in-reagent.routes :as routes]
            [reactrails-in-reagent.subs :as subs]
            [reactrails-in-reagent.handlers :as handlers]))

(defonce prefix (atom ""))

(defmulti page :id)
(defmethod page nil [_]
   [:div "Loading..."])

(defmethod page :default [_]
  [:div [:h1 "Page not found"]])

(defn nav-link [active title url page collapsed?]
  [:li {:class (when (= page (:id active)) "active")}
   [:a {:href url
        :on-click (handler-fn reset! collapsed? true)}
    title]])

(defc navbar [active-page]
  [collapsed? (atom true)]
  [:nav.navbar.navbar-inverse.navbar-fixed-top
   [:div.container
    [:div.navbar-header
     [:button.navbar-toggle
      {:class         (when-not @collapsed? "collapsed")
       :data-toggle   "collapse"
       :aria-expanded @collapsed?
       :aria-controls "navbar"
       :on-click      #(swap! collapsed? not)}
      [:span.sr-only "Toggle Navigation"]
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:a.navbar-brand {:href "#/"} "reactrails-in-reagent"]]
    [:div.navbar-collapse.collapse
     (when-not @collapsed? {:class "in"})
     [:ul.nav.navbar-nav
      [nav-link active-page "Comment - no routing" "/" :non-router-comment-screen collapsed?]]]]])

(defc app []
  []
  (with-subs [active-page [:active-page]]
    [:div
      [navbar active-page]
      (page active-page)]))


(defn non-router-comment-screen []
  [:div
    [:div.container
     [:h2 "Using reagent + re-frame + secretary + clojure backend"]]
    [c/comment-screen]])

(defmethod page :non-router-comment-screen [_] [non-router-comment-screen])


;; -------------------------
;; Initialize app
(defn mount-components []
  (r/render [#'app] (.getElementById js/document "app")))

(defn init! []
  (routes/app-routes @prefix)
  (rf/dispatch-sync [:initialize-db])
  (mount-components))
