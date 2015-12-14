(ns reactrails-in-reagent.cljsmacro
  (:require [clojure.string :as string]))

(defn- to-sub
  [[binding sub]]
  `[~binding (re-frame.core/subscribe ~sub)])

(defn- to-deref
  [binding]
  `[~binding (deref ~binding)])

(defmacro with-subs
  [bindings & body]
  `(let [~@(apply concat (map to-sub (partition 2 bindings)))]
     (fn []
       (let [~@(apply concat (map to-deref (take-nth 2 bindings)))]
         ~@body))))

(defmacro handler-fn
  ([& body]
   `(fn [~'ev] ~@body nil)))

;; nice for reagent
(defmacro defc
  "syntactic sugar for declaring stateful components. Keeps signature of inner
  and outer params consistent.
  Before:
  (defn stateful-component
   [params]
   (let [local state]
     (fn [params])
       body)))
  After:
  (defc stateful-component
   [params]
   [local state]
   body)"
  [component-name params let-block & body]
  (let [inner (concat `(fn ~params) body)]
   `(def ~component-name (fn ~params
                           (let ~let-block ~inner)))))

;; generally useful

(defmacro mirror
  "shorthand for declaring a map where the keys are keywords that correspond to
  the value names. CoffeeScript users might find this as familiar shorthand.
  Before:
  {:foo foo :bar bar}
  After:
  (mirror foo bar)"
  [& syms]
  (apply hash-map (flatten (map (fn [s] [(keyword s) s]) syms))))

(defmacro defm
  "shorthand for defining referentially transparent functions. use in place of
  `defn` for referentially transparent functions.

  (defm add
   [x y]
   (+ x Y)"
  [fn-name params & body]
  (let [inner (concat `(fn ~params) body)]
   `(def ~fn-name (memoize ~inner))))


(defn- to-property [sym]
  (symbol (str "-" sym)))

(defmacro goog-extend [type base-type ctor & methods]
 `(do
    (defn ~type ~@ctor)

    (goog/inherits ~type ~base-type)

    ~@(map
       (fn [method]
         `(set! (.. ~type -prototype ~(to-property (first method)))
                (fn ~@(rest method))))
       methods)))

;; react bootstrap components

(defn capitalize [s]
  (if (< (count s) 2)
    (string/upper-case s)
    (str (string/upper-case (subs s 0 1)) (subs s 1))))

(defn dash-to-camel-up [dashed]
  (let [name-str (name dashed)
        parts (string/split name-str #"-")]
    (apply str (map capitalize parts))))

(defmacro adapt-react-components [root & syms]
  `(do
    ~@(for [sym syms]
       `(def ~sym (reagent.core/adapt-react-class (aget ~root ~(dash-to-camel-up sym)))))))
