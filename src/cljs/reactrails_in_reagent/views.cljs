(ns reactrails-in-reagent.views
  (:require
    [reagent.core :as r]

    [reagent-forms.core :as r-forms]

    [reactrails-in-reagent.dispatch :as d]
    [reactrails-in-reagent.actions :as actions]))

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

(defn state-view [app-state]
  [:div
   (str @app-state)])


(defn comment-entry [c]
  [:li {:key (:db/id c)}
   [:p (:comment/author c)]
   [:p (:comment/text c)]])

(defn comments [comments-list]
  [:ul
   (for [c @comments-list]
     [comment-entry c])])


(defn app [app-state]
  [:div
   [form]
   [comments (r/cursor app-state [:comments])]
   [state-view app-state]
   ])

(defn render! [app-state]
  (r/render-component [app app-state]
                      (.-body js/document)))
