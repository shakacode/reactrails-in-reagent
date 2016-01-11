;; gorilla-repl.fileformat = 1

;; **
;;; # Schemas in RinR
;;; 
;;; In the app we use [Prismatic Schema](https://github.com/Prismatic/schema) to validate and coerce data traveling over the wire.
;; **

;; @@
(ns schemas
  (:require [clojure.repl :refer [source]]
            [clojure.set]
            [schema.core :as s]
            [schema.coerce :as c]
            [schema.utils :as s-utils]
            [cheshire.core :as json]
            [reactrails-in-reagent.comment.schemas :as rinr-schemas]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Prismatic Schema
;;; 
;;; ### Validating Data
;;; [Schema](https://github.com/Prismatic/schema) was first a validation library. So let's use it validate some data.
;;; 
;;; In the reactrails-in reagent app, we can create comments. In the `reacrails-in-reagent.comment.schemas` namespace we declare a schema for a new comment like this:
;; **

;; @@
(s/defschema New-comment {(s/required-key :comment/author)  s/Str
                          (s/required-key :comment/text) s/Str})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/New-comment</span>","value":"#'schemas/New-comment"}
;; <=

;; **
;;; Here we specify that a comment must be a map of 2 keys `:comment/author` and `:comment/text`, and that the values for these keys must be strings.
;;; 
;;; Our Schema is just clojure data:
;; **

;; @@
New-comment
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/author</span>","value":":comment/author"},{"type":"html","content":"<span class='clj-class'>java.lang.String</span>","value":"java.lang.String"}],"value":"[:comment/author java.lang.String]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/text</span>","value":":comment/text"},{"type":"html","content":"<span class='clj-class'>java.lang.String</span>","value":"java.lang.String"}],"value":"[:comment/text java.lang.String]"}],"value":"{:comment/author java.lang.String, :comment/text java.lang.String}"}
;; <=

;; **
;;; Note the use of `s/Str` to specify the schema for a name or a comment. [Schema](https://github.com/Prismatic/schema) being useable in Clojure and ClojureScript, we sometimes can use type 'aliases' that will work for both languages. In these case `s/Str` became `java.clojure.String` since were using  a clojure REPL in this document.
;;; 
;;; We can try to check some comments.
;; **

;; @@
(def c1 {:comment/author "Alice" :comment/text "Hi"})
(def c2 {:comment/text "Hi to you to"})
(def c3 {:comment/author "Bob" :comment/text 42})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/c3</span>","value":"#'schemas/c3"}
;; <=

;; @@
(s/check New-comment c1)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; `c1` complies to the schema `s/check` returns `nil`.
;; **

;; @@
(s/check New-comment c2)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/author</span>","value":":comment/author"},{"type":"html","content":"<span class='clj-symbol'>missing-required-key</span>","value":"missing-required-key"}],"value":"[:comment/author missing-required-key]"}],"value":"{:comment/author missing-required-key}"}
;; <=

;; **
;;; `c2` is missing the the `:comment/author` key.
;; **

;; @@
(s/check New-comment c3)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/text</span>","value":":comment/text"},{"type":"html","content":"<span class='clj-unkown'>(not (instance? java.lang.String 42))</span>","value":"(not (instance? java.lang.String 42))"}],"value":"[:comment/text (not (instance? java.lang.String 42))]"}],"value":"{:comment/text (not (instance? java.lang.String 42))}"}
;; <=

;; **
;;; `c3` doesn't have a string value for the key `:comment/text`.
;;; 
;;; We can also validate data:
;; **

;; @@
(s/validate New-comment c1)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/author</span>","value":":comment/author"},{"type":"html","content":"<span class='clj-string'>&quot;Alice&quot;</span>","value":"\"Alice\""}],"value":"[:comment/author \"Alice\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/text</span>","value":":comment/text"},{"type":"html","content":"<span class='clj-string'>&quot;Hi&quot;</span>","value":"\"Hi\""}],"value":"[:comment/text \"Hi\"]"}],"value":"{:comment/author \"Alice\", :comment/text \"Hi\"}"}
;; <=

;; @@
(try 
  (s/validate New-comment c2)
  (catch Exception e
    (-> e 
        (ex-data )
        (select-keys [:value :error]))))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:value</span>","value":":value"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/text</span>","value":":comment/text"},{"type":"html","content":"<span class='clj-string'>&quot;Hi to you to&quot;</span>","value":"\"Hi to you to\""}],"value":"[:comment/text \"Hi to you to\"]"}],"value":"{:comment/text \"Hi to you to\"}"}],"value":"[:value {:comment/text \"Hi to you to\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:error</span>","value":":error"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/author</span>","value":":comment/author"},{"type":"html","content":"<span class='clj-symbol'>missing-required-key</span>","value":"missing-required-key"}],"value":"[:comment/author missing-required-key]"}],"value":"{:comment/author missing-required-key}"}],"value":"[:error {:comment/author missing-required-key}]"}],"value":"{:value {:comment/text \"Hi to you to\"}, :error {:comment/author missing-required-key}}"}
;; <=

;; **
;;; ## Coercions
;;; 
;;; ### Intro
;;; Under the hood [Schema](https://github.com/Prismatic/schema) implements an algorithm that walks both a schema and a piece of data and compares at each step if the data corresponds to the schema.
;;; 
;;; At some point the developpeurs of the library cleverly realised that this walking of data directed by a schema can do more than just validate data and they introduced coercions [1](#ref1) and [data transformations](https://github.com/Prismatic/schema/wiki/Writing-Custom-Transformations).
;;; 
;;; One problem when dealing with [JSON](http://www.json.org) in Clojure is that JSON is less expressive than [edn](https://github.com/edn-format/edn).
;;; 
;;; In the namespace `reactrails-in-reagent.comment`, where we handle POST requests of a new comments, the new comment is received has [JSON](http://www.json.org). [Cheshire](https://github.com/dakrone/cheshire) our [JSON](http://www.json.org) to [edn](https://github.com/edn-format/edn) parser will get us data ressembling this:
;; **

;; @@
(def received {"author" "Alice" "text" "A comment."})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/received</span>","value":"#'schemas/received"}
;; <=

;; **
;;; And of course:
;; **

;; @@
(s/check schemas/New-comment received)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/author</span>","value":":comment/author"},{"type":"html","content":"<span class='clj-symbol'>missing-required-key</span>","value":"missing-required-key"}],"value":"[:comment/author missing-required-key]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/text</span>","value":":comment/text"},{"type":"html","content":"<span class='clj-symbol'>missing-required-key</span>","value":"missing-required-key"}],"value":"[:comment/text missing-required-key]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;author&quot;</span>","value":"\"author\""},{"type":"html","content":"<span class='clj-symbol'>disallowed-key</span>","value":"disallowed-key"}],"value":"[\"author\" disallowed-key]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;text&quot;</span>","value":"\"text\""},{"type":"html","content":"<span class='clj-symbol'>disallowed-key</span>","value":"disallowed-key"}],"value":"[\"text\" disallowed-key]"}],"value":"{:comment/author missing-required-key, :comment/text missing-required-key, \"author\" disallowed-key, \"text\" disallowed-key}"}
;; <=

;; **
;;; Instead of coercing and then using schema to validate this comment, we can use  [Schema](https://github.com/Prismatic/schema)'s coercions to do both.
;;; 
;;; ### Coercers in RinR
;;; 
;;; In the reactrails in Reagent app we use one coercer in the backend to reformat and validate new comments that are posted (namespace `reactrails-in-reagent.comment`) and one on the client side to do the same for comments coming from the server.
;;; 
;;; The client usually receives something like this:
;; **

;; @@
(def comment-json (json/generate-string {:comment/author "An author" 
                                         :comment/text "Somme text"
                                         :comment/created (java.util.Date. )
                                         :db/id 8765432}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/comment-json</span>","value":"#'schemas/comment-json"}
;; <=

;; @@
comment-json
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;{\\&quot;comment/author\\&quot;:\\&quot;An author\\&quot;,\\&quot;comment/text\\&quot;:\\&quot;Somme text\\&quot;,\\&quot;comment/created\\&quot;:\\&quot;2016-01-11T18:15:43Z\\&quot;,\\&quot;db/id\\&quot;:8765432}&quot;</span>","value":"\"{\\\"comment/author\\\":\\\"An author\\\",\\\"comment/text\\\":\\\"Somme text\\\",\\\"comment/created\\\":\\\"2016-01-11T18:15:43Z\\\",\\\"db/id\\\":8765432}\""}
;; <=

;; **
;;; Once parsed the client recovers this:
;; **

;; @@
(def comment-client-side (json/decode comment-json))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/comment-client-side</span>","value":"#'schemas/comment-client-side"}
;; <=

;; @@
comment-client-side
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;comment/author&quot;</span>","value":"\"comment/author\""},{"type":"html","content":"<span class='clj-string'>&quot;An author&quot;</span>","value":"\"An author\""}],"value":"[\"comment/author\" \"An author\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;comment/text&quot;</span>","value":"\"comment/text\""},{"type":"html","content":"<span class='clj-string'>&quot;Somme text&quot;</span>","value":"\"Somme text\""}],"value":"[\"comment/text\" \"Somme text\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;comment/created&quot;</span>","value":"\"comment/created\""},{"type":"html","content":"<span class='clj-string'>&quot;2016-01-11T18:15:43Z&quot;</span>","value":"\"2016-01-11T18:15:43Z\""}],"value":"[\"comment/created\" \"2016-01-11T18:15:43Z\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;db/id&quot;</span>","value":"\"db/id\""},{"type":"html","content":"<span class='clj-unkown'>8765432</span>","value":"8765432"}],"value":"[\"db/id\" 8765432]"}],"value":"{\"comment/author\" \"An author\", \"comment/text\" \"Somme text\", \"comment/created\" \"2016-01-11T18:15:43Z\", \"db/id\" 8765432}"}
;; <=

;; **
;;; This isn't quite what we want. Let's take a look at what is in our schemas for the app.
;; **

;; @@
(source rinr-schemas/New-comment)
(println)

(source rinr-schemas/Comment)
(println)

(source rinr-schemas/date-schema)
(println)

(source rinr-schemas/long-schema)
(println)
;; @@
;; ->
;;; (s/defschema New-comment {(s/required-key :comment/author)  s/Str
;;;                           (s/required-key :comment/text) s/Str})
;;; 
;;; (s/defschema Comment (merge New-comment
;;;                             {(s/required-key :comment/created) date-schema
;;;                              (s/required-key :db/id) long-schema}))
;;; 
;;; (def date-schema #?(:clj java.util.Date
;;;                     :cljs js/Date))
;;; 
;;; (def long-schema #?(:clj Long
;;;                     :cljs js/Number))
;;; 
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We see that we want, for a comment, a map with 4 required keys:
;;; 
;;; * :comment/author  &rarr; a `s/Str` name
;;; * :comment/text    &rarr; a `s/str` comment
;;; * :comment/created &rarr; a `date-schema` date of creation
;;; * :db/id           &rarr; a `long-schema` db id 
;;; 
;;; 
;; **

;; **
;;; To coerce the comment into we use [Schema](https://github.com/Prismatic/schema)'s coercers.
;;; 
;;; First we define a function to replace the string keys of the received comment with the correct keywords:
;; **

;; @@
(defn comment-coercion [comment]
  (clojure.set/rename-keys comment {"comment/author"  :comment/author
                                    "comment/text"    :comment/text
                                    "comment/created" :comment/created
                                    "db/id"           :db/id}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/comment-coercion</span>","value":"#'schemas/comment-coercion"}
;; <=

;; **
;;; Then we can define a coercion matcher:
;; **

;; @@
(def comment-matcher {rinr-schemas/Comment     comment-coercion
                      rinr-schemas/date-schema rinr-schemas/date-coercion
                      rinr-schemas/long-schema rinr-schemas/long-coercion})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/comment-matcher</span>","value":"#'schemas/comment-matcher"}
;; <=

;; **
;;; A coercion matcher is a function that returns a coercion function given a schema. Here we take advantage of the fact that clojure maps are also functions and create a map of schemas to coercion fns as our matcher.
;;; 
;;; Then we can 'compile' our coercer using the `schema.coerce/coercer` function:
;; **

;; @@
(def comment-coercer 
  (c/coercer rinr-schemas/Comment
             comment-matcher))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/comment-coercer</span>","value":"#'schemas/comment-coercer"}
;; <=

;; **
;;; and use it to coerce the data received by the client:
;; **

;; @@
(comment-coercer comment-client-side)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/author</span>","value":":comment/author"},{"type":"html","content":"<span class='clj-string'>&quot;An author&quot;</span>","value":"\"An author\""}],"value":"[:comment/author \"An author\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/text</span>","value":":comment/text"},{"type":"html","content":"<span class='clj-string'>&quot;Somme text&quot;</span>","value":"\"Somme text\""}],"value":"[:comment/text \"Somme text\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/created</span>","value":":comment/created"},{"type":"html","content":"<span class='clj-unkown'>#inst &quot;2016-01-11T18:15:43.000-00:00&quot;</span>","value":"#inst \"2016-01-11T18:15:43.000-00:00\""}],"value":"[:comment/created #inst \"2016-01-11T18:15:43.000-00:00\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:db/id</span>","value":":db/id"},{"type":"html","content":"<span class='clj-long'>8765432</span>","value":"8765432"}],"value":"[:db/id 8765432]"}],"value":"{:comment/author \"An author\", :comment/text \"Somme text\", :comment/created #inst \"2016-01-11T18:15:43.000-00:00\", :db/id 8765432}"}
;; <=

;; **
;;; Now we have data we can use.
;;; 
;;; It is important to remember that our coercer also validates the data:
;; **

;; @@
(def bad-comment (dissoc comment-client-side "db/id"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/bad-comment</span>","value":"#'schemas/bad-comment"}
;; <=

;; @@
(comment-coercer bad-comment)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:error</span>","value":":error"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:db/id</span>","value":":db/id"},{"type":"html","content":"<span class='clj-symbol'>missing-required-key</span>","value":"missing-required-key"}],"value":"[:db/id missing-required-key]"}],"value":"{:db/id missing-required-key}"}],"value":"[:error {:db/id missing-required-key}]"}],"value":"#schema.utils.ErrorContainer{:error {:db/id missing-required-key}}"}
;; <=

;; @@
(s-utils/error? (comment-coercer bad-comment))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>true</span>","value":"true"}
;; <=

;; **
;;; We could also include the json parsing operation in our coercer:
;; **

;; @@
(def comment-matcher-with-parse (update comment-matcher 
                                        rinr-schemas/Comment 
                                        #(comp % json/decode)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/comment-matcher-with-parse</span>","value":"#'schemas/comment-matcher-with-parse"}
;; <=

;; @@
(def comment-coercer-with-json-parse
  (c/coercer rinr-schemas/Comment comment-matcher-with-parse))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;schemas/comment-coercer-with-json-parse</span>","value":"#'schemas/comment-coercer-with-json-parse"}
;; <=

;; @@
comment-json
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;{\\&quot;comment/author\\&quot;:\\&quot;An author\\&quot;,\\&quot;comment/text\\&quot;:\\&quot;Somme text\\&quot;,\\&quot;comment/created\\&quot;:\\&quot;2016-01-11T18:15:43Z\\&quot;,\\&quot;db/id\\&quot;:8765432}&quot;</span>","value":"\"{\\\"comment/author\\\":\\\"An author\\\",\\\"comment/text\\\":\\\"Somme text\\\",\\\"comment/created\\\":\\\"2016-01-11T18:15:43Z\\\",\\\"db/id\\\":8765432}\""}
;; <=

;; @@
(comment-coercer-with-json-parse comment-json)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/author</span>","value":":comment/author"},{"type":"html","content":"<span class='clj-string'>&quot;An author&quot;</span>","value":"\"An author\""}],"value":"[:comment/author \"An author\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/text</span>","value":":comment/text"},{"type":"html","content":"<span class='clj-string'>&quot;Somme text&quot;</span>","value":"\"Somme text\""}],"value":"[:comment/text \"Somme text\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:comment/created</span>","value":":comment/created"},{"type":"html","content":"<span class='clj-unkown'>#inst &quot;2016-01-11T18:15:43.000-00:00&quot;</span>","value":"#inst \"2016-01-11T18:15:43.000-00:00\""}],"value":"[:comment/created #inst \"2016-01-11T18:15:43.000-00:00\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:db/id</span>","value":":db/id"},{"type":"html","content":"<span class='clj-long'>8765432</span>","value":"8765432"}],"value":"[:db/id 8765432]"}],"value":"{:comment/author \"An author\", :comment/text \"Somme text\", :comment/created #inst \"2016-01-11T18:15:43.000-00:00\", :db/id 8765432}"}
;; <=

;; **
;;; <a name="ref1"></a>[1] see the in the [schema page](https://github.com/Prismatic/schema) the section 'Transformations and Coercion'
;; **
