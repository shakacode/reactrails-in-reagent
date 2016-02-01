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


(def date-coercion #?(:clj  instant/read-instant-date
                      :cljs #(js/Date. %)))

(defn try-apply [f & args]
  (try
    (apply f args)
    (catch #?(:clj Exception :cljs :default) _
      nil)))

#?(:clj
   (defn parse-long [s]
     (Long/parseLong s)))

(def long-coercion
  #?(:clj (coerce/first-matcher [#(try-apply long %)
                                 #(try-apply parse-long %)])
     :cljs js/Number))
