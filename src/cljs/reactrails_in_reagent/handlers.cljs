(ns reactrails-in-reagent.handlers
    (:require [re-frame.core :as rf]
              [reactrails-in-reagent.api :as api]))

(rf/register-handler
 :initialize-db
 (fn [_ _]
   {:is-fetching false
    :is-saving false
    :fetch-comment-error nil
    :submit-comment-error nil
    :comments []}))

(rf/register-handler
 :set-active-page
 ; rf/debug
 (fn [db [_ active-page params]]
   (assoc db :active-page {:id active-page :params params})))

(rf/register-handler
  :fetch-comments
  ; rf/debug
  (fn [db _]
    (api/get-comments #(rf/dispatch [:fetch-comments-success %]) #(rf/dispatch [:fetch-comments-failure (:failure %)]))
    (assoc db :is-fetching true)))

(rf/register-handler
  :fetch-comments-success
  (fn [db [_ comments]]
    (-> db
        (assoc :fetch-comment-error nil)
        (assoc :is-fetching nil)
        (assoc :comments comments))))

(rf/register-handler
  :fetch-comments-failure
  (fn [db [_ error]]
    (-> db
        (assoc :fetch-comment-error error)
        (assoc :is-fetching nil))))


(rf/register-handler
  :submit-comment
  (fn [db [_ comment]]
    (api/save-comment comment #(rf/dispatch [:submit-comment-success %]) #(rf/dispatch [:submit-comment-failure (:failure %)]))
    (assoc db :is-saving true)))

(rf/register-handler
  :submit-comment-success
  ; rf/debug
  (fn [db [_ comment]]
    (-> db
      (assoc :submit-comment-error nil)
      (assoc :is-saving nil)
      (update :comments conj comment))))

(rf/register-handler
  :submit-comment-failure
  (fn [db [_ error]]
    (-> db
        (assoc :submit-comment-error error)
        (assoc :is-saving nil))))
