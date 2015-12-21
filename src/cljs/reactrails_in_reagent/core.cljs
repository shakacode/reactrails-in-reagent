(ns reactrails-in-reagent.core
  (:require
    [reagent.core :as r]

    [reagent-forms.core :as r-forms]

    [reactrails-in-reagent.dispatch :as d]
    [reactrails-in-reagent.actions :as actions]))

(enable-console-print!)

(defn on-js-reload []
  (.log js/console "reloaded!!!"))


(def initial-state {:comments []})

(defonce app-state (r/atom initial-state))
(def comments-list (r/cursor app-state [:comments]))



(defn form-template [doc]
  [:form
   [:label {:for :comment/author} "Your name:"] [:input {:field :text :id :comment/author }]
   [:label {:for :comment/text} "Your comment:"] [:textarea {:field :textarea :id :comment/text}]

   [:input {:type "submit"
            :value "send"
            :on-click (fn [e]
                        (.preventDefault e)
                        (println "been there")
                        (d/dispatch! (actions/->NewComment @doc)))}]])

(defn form []
  (let [doc (r/atom {})]
    (fn []
      [:div
       [r-forms/bind-fields (form-template doc) doc]])))

(defn state-view []
  [:div
   (str @app-state)])


(defn comment-entry [c]
  [:li
   [:p (:comment/author c)]
   [:p (:comment/text c)]])

(defn comments [comments-list]
  [:ul
   (for [c @comments-list]
     [comment-entry c])])


(defn app []
  [:div
   [form]
   [comments comments-list]
   [state-view]
   ])

(defn render! []
  (r/render-component [app]
                      (.-body js/document)))



(defn main [& args]
  (reset! app-state initial-state)
  (render!)
  (d/start-dispatcher! app-state))


(defonce started (main))
