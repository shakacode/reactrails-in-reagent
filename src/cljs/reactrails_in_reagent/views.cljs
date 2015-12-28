(ns reactrails-in-reagent.views
  (:require
    [reagent.core :as r]

    [reagent-forms.core :as r-forms]

    [reactrails-in-reagent.dispatch :as d]
    [reactrails-in-reagent.actions :as actions]

    [cljs.pprint :as pp]

    ))




(defn submit [event doc-atom]
  (.preventDefault event)
  (d/dispatch! (actions/->NewComment @doc-atom))
  (reset! doc-atom {}))


(def input-name
  [:input {:field :text
           :type "text"
           :id :author
           :name "author"
           :placeholder "Your Name"
           :class "form-control"}])

(def comment-textarea
  [:textarea {:field :textarea
              :id :text
              :placeholder "Say something using Markdown"
              :class "form-control" :name "text"}])


(defn template-form-horizontal [doc]
  [:form {:class    "commentForm form-horizontal"
          :onSubmit #(submit % doc)}
   [:div {:class "form-group"}
    [:label {:class "control-label col-sm-2"} "Name"]
    [:div {:class "col-sm-10"}
     input-name]]
   [:div {:class "form-group"}
    [:label {:class "control-label col-sm-2"} "Text"]
    [:div {:class "col-sm-10"}
     comment-textarea]]
   [:div {:class "form-group"}
    [:div {:class "col-sm-offset-2 col-sm-10"}
     [:input {:type "submit" :class "btn btn-primary" :value "Post"}]]]])


(defn template-form-stacked [doc]
  [:form {:class    "commentForm "
          :onSubmit #(submit % doc)}
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Name"]
    input-name]
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Text"]
    comment-textarea]
   [:input {:type "submit" :class "btn btn-primary" :value "Post"}]])

(defn template-form-inline [doc]
  [:form {:class "commentForm" :onSubmit #(submit % doc)}
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Inline Form"]
    [:div {:class "wrapper"}
     [:div {:class "row"}
      [:div {:class "col-xs-3"}
       input-name]
      [:div {:class "col-xs-8"}
       comment-textarea]
      [:div {:class "col-xs-1"}
       [:input {:type "submit" :class "btn btn-primary" :value "Post"}]]]]]])



(defn form-style-selector [index]
  [:nav
   [:ul {:class "nav nav-pills"}
    [:li (if (= 0 index) {:class "active"})
     [:a {:onClick #(d/dispatch! (actions/->SelectFormStyle 0))}
      "Horizontal Form"]]
    [:li (if (= 1 index) {:class "active"})
     [:a {:onClick #(d/dispatch! (actions/->SelectFormStyle 1))}
      "Stacked Form"]]
    [:li (if (= 2 index) {:class "active"})
     [:a {:onClick #(d/dispatch! (actions/->SelectFormStyle 2))}
      "Inline Form"]]]])


(let [doc (r/atom {})]
  (defn form-horizontal []
    (fn []
      [r-forms/bind-fields (template-form-horizontal doc) doc]))

  (defn form-stacked []
    (fn []
      [r-forms/bind-fields (template-form-stacked doc) doc]))

  (defn form-inline []
    (fn []
      [r-forms/bind-fields (template-form-inline doc) doc])))


(defn form [index]
  (let [form (case @index
               0 form-horizontal
               1 form-stacked
               2 form-inline)]
    [:div [form]]))

(defn state-view [app-state]
  [:pre
   (with-out-str (pp/pprint @app-state))])


(defn comment-view [c]
  [:li {:key (:db/id c) :class "comment" }
   [:h2 {:class "comment-author"} (:comment/author c)]
   [:span {:class "comment-text"
           :dangerouslySetInnerHTML
                  {:__html (-> c :comment/text str js/marked)}}]])


(defn comments [comments-list]
  [:ul
   (for [c @comments-list]
     (comment-view c))])


(defn app [app-state]
  [:div
   [form-style-selector (:nav/index @app-state)]
   [form (r/cursor app-state [:nav/index])]
   [comments (r/cursor app-state [:comments])]
   ;[state-view app-state]
   ])

(defn render! [app-state]
  (r/render-component [app app-state]
                      (.-body js/document)))


