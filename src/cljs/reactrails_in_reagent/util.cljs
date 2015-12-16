(ns reactrails-in-reagent.util)

(defn starts-with
  "check whether string s startsWith start"
  [s start]
  (js* "s['indexOf'](start)===0"))
