(ns reactrails-in-reagent.comment.schemas
  (:require
    [schema.core :as s]
    [schema.coerce :as coerce]
    [clojure.set]

    #?(:clj [clojure.instant :as instant])))

(s/defschema New-comment {(s/required-key :comment/author)  s/Str
                          (s/required-key :comment/text) s/Str})


(def date-schema #?(:clj java.util.Date
                    :cljs js/Date))

(def long-schema #?(:clj Long
                    :cljs js/Number))

(s/defschema Comment (merge New-comment
                            {(s/required-key :comment/created) date-schema
                             (s/required-key :db/id) long-schema}))

(s/defschema Comment-list [Comment])


(def date-matcher #?(:clj  instant/read-instant-date
                     :cljs #(js/Date. %)))
