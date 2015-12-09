(ns reactrails-in-reagent.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {:author ""
                      :nav 0
                      :comments [{:author "Bob" :text "The Price Is Right!"}] }))

(defn add-comment! [c]
  (swap! app-state update :comments (comp reverse   #(conj % )) c )

(defn handle-submit [e]
  (.preventDefault e)
  (let [author (.-value (.getElementById js/document   "author"))
        text (.-value (.getElementById js/document "text"))]
    (aset  (.getElementById js/document "text") "value" "") ;resets textaraa
    (add-comment! {:author author :text text}))))

(defn handle-nav-change [index]
  (swap! app-state assoc :nav index))

(defn handle-author-change [e]
  (let [author (-> e .-target .-value)]
    (swap! app-state assoc :author author)))

(defn Form-horizontal []
  [:form {:class "commentForm form-horizontal" :onSubmit handle-submit}
   [:div {:class "form-group"}
    [:label {:class "control-label col-sm-2"} "Name"]
    [:div {:class "col-sm-10"}
     [:input {:type "text" :id "author" :name "author" :placeholder "Your Name"
              :value (:author @app-state) :onChange handle-author-change :class "form-control"}]]]
   [:div {:class "form-group"}
    [:label {:class "control-label col-sm-2"} "Text"]
    [:div {:class "col-sm-10"}
     [:input {:type "textarea" :id "text" :placeholder "Say something using Markdown"
              :class "form-control" :name "text"}]]]
   [:div {:class "form-group"}
    [:div {:class "col-sm-offset-2 col-sm-10"}
     [:input {:type "submit" :class "btn btn-primary" :value "Post"}]]]])

(defn Form-stacked []
  [:form {:class "commentForm " :onSubmit handle-submit}
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Name"]
    [:input {:type "text" :id "author" :name "author" :placeholder "Your Name" :class "form-control"}]]
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Text"]
    [:input {:type "textarea" :id "text" :placeholder "Say something using Markdown"
             :class "form-control" :name "text"}]]
   [:input {:type "submit" :class "btn btn-primary" :value "Post"}]])

(defn Form-inline []
  [:form {:class "commentForm " :onSubmit handle-submit}
   [:div {:class "form-group"}
    [:label {:class "control-label "} "Inline Form"]
    [:div {:class "wrapper"}
     [:div {:class "row"}
      [:div {:class "col-xs-3"}
       [:input {:type "text" :id "author" :name "author" :placeholder "Your Name" :class "form-control"}]]
      [:div {:class "col-xs-8"}
       [:input {:type "textarea" :id "text" :placeholder "Say something using Markdown"
                :class "form-control" :name "text"}]]
      [:div {:class "col-xs-1"}
       [:input {:type "submit" :class "btn btn-primary" :value "Post"}]]]]]])

(defn comment-form [index]
  (condp = index
    0 (Form-horizontal)
    1 (Form-stacked)
    2 (Form-inline)))

(defn Comment [{:keys [author text]}]
  [:div {:class "comment" }
   [:h2 {:class "comment-author"} author]
   [:span {:class "comment-text"
           :dangerouslySetInnerHTML
           {:__html (-> text str js/marked)}}]])

(defn Nav [index]
  [:nav
   [:ul {:class "nav nav-pills"}
    [:li (if (= 0 index) {:class "active"})
     [:a {:onClick #(handle-nav-change 0)} "Horizontal Form"]]
    [:li (if (= 1 index) {:class "active"})
     [:a {:onClick #(handle-nav-change 1)} "Stacked Form"]]
    [:li (if (= 2 index) {:class "active"})
     [:a {:onClick #(handle-nav-change 2)} "Inline Form"]]]])

(defn App []
  [:div
   [:h1 "Reagent-wrapped React with Rails on backend"]
   [:p "Hot-reloading brought to you by Figwheel"]
   [:div {:class "comment-box"}
    [:h1 "Comments"]
    [:p "Text takes Github Flavored Markdown. Comments older than 24 hours are
    deleted." [:br][:b "Name"] " is preserved." [:b "Text"] " is reset, between submits."]
    [:div {:class "commentBox container"}
     (Nav (:nav @app-state))
     [:hr]
     (comment-form (:nav @app-state))
     (for [comment-item (:comments @app-state)]
       (Comment comment-item))]]])

(reagent/render-component [App]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
