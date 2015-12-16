(ns reactrails-in-reagent.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as rf]))

(rf/register-sub
 :active-page
 (fn [db _]
   (reaction (:active-page @db))))

(rf/register-sub
  :comments
  (fn [db _]
    ; this is not efficient for large lists, instead of reversing list,
    ; rendering loop should be done in reverse
    (reaction (reverse (get @db :comments)))))

(rf/register-sub
 :db-path
 (fn [db [_ path]]
   (reaction (get-in @db path))))
