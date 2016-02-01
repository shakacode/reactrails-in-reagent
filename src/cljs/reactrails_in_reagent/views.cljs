(ns reactrails-in-reagent.views
  (:require
    [reagent.core :as r]

    [reagent-forms.core :as r-forms]

    [reactrails-in-reagent.dispatch :as d]
    [reactrails-in-reagent.actions :as actions]

    [cljs.pprint :as pp]

    ))


(defn wrap-prevent-default [event-handler]
  (fn [event]
    (.preventDefault event)
    (event-handler event)))

(defn submit-fn [!doc]
  (wrap-prevent-default
    (fn [_]
      (d/dispatch! (actions/->NewComment @!doc))
      (reset! !doc {}))))

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

;; The forms here are an adaptation to reagent of the the work of monjohn
;; https://github.com/shakacode/reactrails-in-om-next-example/blob/monjohn-master/src/cljs/omnext_to_datomic/components.cljs
(defn template-form-horizontal [!doc]
  [:form {:class    "commentForm form-horizontal"
          :onSubmit (submit-fn !doc)}
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


(defn template-form-stacked [!doc]
  [:form {:class    "commentForm "
          :onSubmit (submit-fn !doc)}
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Name"]
    input-name]
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Text"]
    comment-textarea]
   [:input {:type "submit" :class "btn btn-primary" :value "Post"}]])

(defn template-form-inline [!doc]
  [:form {:class "commentForm"
          :onSubmit (submit-fn !doc)}
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
    [:li (if (= :horizontal index) {:class "active"})
     [:a {:onClick (wrap-prevent-default
                     #(d/dispatch! (actions/->SelectFormStyle :horizontal)))}
      "Horizontal Form"]]
    [:li (if (= :stacked index) {:class "active"})
     [:a {:onClick (wrap-prevent-default
                     #(d/dispatch! (actions/->SelectFormStyle :stacked)))}
      "Stacked Form"]]
    [:li (if (= :inline index) {:class "active"})
     [:a {:onClick (wrap-prevent-default
                     #(d/dispatch! (actions/->SelectFormStyle :inline)))}
      "Inline Form"]]]])



(defn form-horizontal [doc]
  (fn []
    [r-forms/bind-fields (template-form-horizontal doc) doc]))

(defn form-stacked [doc]
  (fn []
    [r-forms/bind-fields (template-form-stacked doc) doc]))

(defn form-inline [doc]
  (fn []
    [r-forms/bind-fields (template-form-inline doc) doc]))

(defn make-form-state [app-state]
  (r/cursor app-state [::comment-form]))

(defn form [app-state]
  (let [form-view (case (:nav/index @app-state)
               :horizontal form-horizontal
               :stacked form-stacked
               :inline form-inline
               form-horizontal)]
    [:div [form-view (make-form-state app-state)]]))


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
   [form app-state]
   [comments (r/cursor app-state [:comments])]
   ])

(defn render! [app-state]
  (r/render-component [app app-state]
                      (.getElementById js/document "app")))


