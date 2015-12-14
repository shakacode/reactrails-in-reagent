(ns reactrails-in-reagent.common
  (:require-macros [reactrails-in-reagent.cljsmacro :refer [adapt-react-components defc handler-fn with-subs]])
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [cljsjs.react-bootstrap :as rb]))

(adapt-react-components js/ReactBootstrap tab tabs nav nav-item row col alert)
(def css-transition-group
  (r/adapt-react-class js/React.addons.CSSTransitionGroup))


(defn map-keys-indexed
  "return sequence of reagent forms with ^key metadata which is index of item in sequence"
  [f items]
  (doall (map-indexed (fn [ix d] (with-meta (f ix d) {:key ix})) items)))

(defn map-keys-prop
  "return sequence of reagent forms with ^key metadata which is (:prop item) in sequence"
  [f prop items]
  (map (fn [d] (with-meta (f d) {:key (prop d)})) items))

(defn input [{:keys [type label placeholder class label-class-name wrapper-class-name ref value on-change disabled]}]
  (fn [{:keys [type label placeholder class label-class-name wrapper-class-name ref value on-change disabled]}]
    [:div.form-group
     [:label.control-label {:class label-class-name} label]
     [:div {:class wrapper-class-name}
       (condp = (name type)
         "text" [:input.form-control {:class class :type type :placeholder placeholder :value value :on-change on-change :disabled disabled}]
         "textarea" [:textarea.form-control {:class class :placeholder placeholder :value value :on-change on-change :disabled disabled}])]]))

(defn nav1 [{:keys [bs-style active-key :on-select]} & children]
  [:ul.nav {:class (str "nav-" (name bs-style))}
    (map-keys-indexed (fn [ix c] c) children)])

(defn nav-item1 [{:keys [active-key event-key]} title]
  (fn [{:keys [active-key event-key]} title]
    [:li {:role "presentation"
          :class (when (= event-key @active-key) "active")}
     [:a {:href ""
            :on-click (handler-fn (.preventDefault ev) (reset! active-key event-key))}
        title]]))

(defn comment-form [is-saving error]
  (let [mode (r/atom 0)
        comment (r/atom {})
        handle-change (fn [prop] (handler-fn (swap! comment assoc prop (-> ev .-target .-value))))
        handle-submit (handler-fn (rf/dispatch [:submit-comment @comment]))]
    (r/create-class
      {:component-did-update (fn [this [_ old-is-saving old-error]]
                                 (let [[_ is-saving error] (r/argv this)]
                                   (when (and old-is-saving (not is-saving) (not error))
                                     (swap! comment dissoc :content)
                                     (when-let [node (.querySelector (r/dom-node this) ".focus-on-new")]
                                        (.focus node)))))
       :reagent-render
       (fn [is-saving error]
        [:div
          [css-transition-group {:transition-name "element" :transition-enter-timeout 300 :transition-leave-timeout 300}
            (when error
              [alert {:bs-style "danger" :key "commentSubmissionError"}
               [:strong "Your comment was not saved!"]
               " A server error prevented your comment from being saved. Please try again."])]
          [nav {:bs-style "pills" :active-key @mode :on-select #(reset! mode %)}
            [nav-item {:active-key mode :event-key 0} "Horizontal Form"]
            [nav-item {:active-key mode :event-key 1} "Stacked Form"]
            [nav-item {:active-key mode :event-key 2} "Inline Form"]]
          (case @mode
              0 [:div [:hr]
                 [:form.commentForm.form-horizontal
                   [input {:type "text" :label "Name" :placeholder "Your Name" :label-class-name "col-sm-2" :wrapper-class-name "col-sm-10"
                           :value (:name @comment) :on-change (handle-change :name) :disabled is-saving}]
                   [input {:type "textarea" :label "Text" :placeholder "Say something using markdown..." :class "focus-on-new" :label-class-name "col-sm-2" :wrapper-class-name "col-sm-10"
                           :value (:content @comment) :on-change (handle-change :content) :disabled is-saving}]
                   [:div.form-group>div.col-sm-offset-2.col-sm-10
                      [:button.btn.btn-primary {:type "button" :disabled is-saving :on-click handle-submit} (if is-saving "Saving..." "Post")]]]]
              1 [:div [:hr]
                  [:form.commentForm.form
                    [input {:type "text" :label "Name" :placeholder "Your Name"
                            :value (:name @comment) :on-change (handle-change :name) :disabled is-saving}]
                    [input {:type "textarea" :label "Text" :placeholder "Say something using markdown..." :class "focus-on-new"
                            :value (:content @comment) :on-change (handle-change :content) :disabled is-saving}]
                    [:button.btn.btn-primary {:type "button" :disabled is-saving :on-click handle-submit} (if is-saving "Saving..." "Post")]]]
              2 [:div [:hr]
                  [:form.commentForm.form
                    [:div.form-group
                      [:label.control-label "Inline Form"]
                      [:div.wrapper
                       [row
                        [col {:xs 3}
                          [:input {:type "text" :label "Name" :placeholder "Your Name" :class "form-control"
                                   :value (:name @comment) :on-change (handle-change :name) :disabled is-saving}]]
                        [col {:xs 8}
                          [:input {:type "text" :label "Text" :placeholder "Say something using markdown..." :class "form-control focus-on-new"
                                   :value (:content @comment) :on-change (handle-change :content) :disabled is-saving}]]
                        [col {:xs 1}
                          [:button.btn.btn-primary {:type "button" :disabled is-saving :on-click handle-submit} (if is-saving "Saving..." "Post")]]]]]]]
              "???")])})))


(defn comment-item [author text]
  [:div.comment
   [:h2.comment-author author]
   [:span.comment-text {:dangerouslySetInnerHTML {:__html (md->html text)}}]])

; TODO: animation
(defn comment-list [data error]
  [:div
    [css-transition-group {:transition-name "element" :transition-enter-timeout 300 :transition-leave-timeout 300}
      (when error
        [alert {:bs-style "danger" :key "commentFetchError"}
          [:strong "Comments could not be retrieved."]
          " A server error prevented loading comments. Please try again."])]
    [css-transition-group {:transition-name "element" :transition-enter-timeout 300 :transition-leave-timeout 300 :class-name "commentList" :component "div"}
      (map-keys-prop (fn [x] [comment-item (:name x) (:content x)]) :id data)]])

(defn comment-box [poll-interval]
  (let [interval-id (r/atom nil)]
    (r/create-class
      {:component-will-mount (fn [] (reset! interval-id
                                      (js/setInterval #(rf/dispatch [:fetch-comments]) poll-interval))
                                    (rf/dispatch [:fetch-comments]))
       :component-will-unmount (fn [] (when @interval-id
                                        (js/clearInterval @interval-id)))
       :reagent-render
        (fn [poll-interval]
          (let [is-fetching (rf/subscribe [:db-path [:is-fetching]])
                is-saving (rf/subscribe [:db-path [:is-saving]])
                submit-comment-error (rf/subscribe [:db-path [:submit-comment-error]])
                fetch-comment-error (rf/subscribe [:db-path [:fetch-comment-error]])
                comments (rf/subscribe [:comments])]
            [:div.commentBox.container
             [:h2 "Comments" (when @is-fetching " Loading...")]
             [:p
              "Text takes Github Flavored Markdown. Comments older than 24 hours are deleted."
              [:br]
              [:b "Name"] " is preserved." [:b "Text"] " is reset between submits"
              [comment-form @is-saving @submit-comment-error]
              [comment-list @comments @fetch-comment-error]]]))})))

(defn comment-screen []
  [:div
    [comment-box 10000]
    [:div.container
     [:a {:href "http://www.railsonmaui.com"}
      [:h3.open-sans-light
        [:div.logo]
        "Example of styling using image-url and Open Sans Light custom font"]
      [:a {:href "https://twitter.com/railsonmaui"}
        [:div.twitter-image
          "Rails On Maui on Twitter"]]]]])
