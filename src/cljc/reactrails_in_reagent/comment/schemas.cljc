(ns reactrails-in-reagent.comment.schemas
  (:require
    [schema.core :as s]
    [schema.coerce :as coerce]
    [clojure.set]))

(s/defschema New-comment {(s/required-key :comment/author)  s/Str
                          (s/required-key :comment/text) s/Str})



