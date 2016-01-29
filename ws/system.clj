;; gorilla-repl.fileformat = 1

;; **
;;; # Components & Systems in RinR
;;; 
;;; In `reactrails-in-reagent` we use Stuart Sierra's [component](https://github.com/stuartsierra/component) library to structure the different parts of the backend. The [readme](https://github.com/stuartsierra/component/blob/master/README.md) of the library describes perfectly what it does:
;;; 
;;; > 'Component' is a tiny Clojure framework for managing the lifecycle and dependencies of software components which have runtime state.
;;; 
;;; > This is primarily a design pattern with a few helper functions. It can be seen as a style of dependency injection using immutable data structures.
;;; 
;;; When working with [component](https://github.com/stuartsierra/component) we need to understand three concepts: *components*, *systems* and *lifecycle*.
;;; 
;;; *Components* will handle the lifecycle of a single piece of state. We will for instance use a component to hold a database connection or an instance of a webserver. 
;;; 
;;; *Systems* are a combination of components and possible dependencies between them. We can for instance make a system of a database component and a webserver component, declaring that the webserver depends on the database. This way we ensure that we have a db connection before starting serving web request.
;;; 
;;; By *lifecycle* and more percisely the lifecycle of something, we simply mean that our components and systems can be started and stopped. 
;;; When starting, a databse component will create a connection, a webserver will start listening to a port and serving requests, a system will start each of it's composing components following dependencies between them.
;;; 
;;; The `reactrails-in-reagent` project is a good example of the use of components. [Component](https://github.com/stuartsierra/component) is at the core of its execution.
;; **

;; @@
(ns systems
  (:require 
    [reactrails-in-reagent.core :as core]
    [reactrails-in-reagent.datomic :as datomic]
    [reactrails-in-reagent.server :as server]
    [reactrails-in-reagent.handler :as handler]
    [reactrails-in-reagent.system :as system]
    [reactrails-in-reagent.comment :as comments]
    [dev.handler]
    [dev.system]
    [clojure.repl :refer [source]]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## RinR production system
;;; 
;;; When we start the app, the `-main` function in the `reactrails-in-reagent.core` namespace is called:
;;; 
;; **

;; @@
(source core/-main)
;; @@
;; ->
;;; (defn -main [&amp; _]
;;;   (let [conf (system/config)
;;;         sys (system/make-system conf)]
;;;     (component/start sys)))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; All we do here is:
;;; 
;;; 1. fetch the configuration for the app: `(system/config)`
;;; 2. create a system using this configuration: `(system/make-system conf)`
;;; 3. start the system: `(component/start sys)`.
;;; 
;;; Here is the function fetching the config:
;; **

;; @@
(source system/config)
;; @@
;; ->
;;; (defn config []
;;;   {:db-uri         &quot;datomic:mem://example&quot;
;;;    :schema         (read-edn-ressource &quot;data/schema.edn&quot;)
;;;    :server-config  {:port (Integer/parseInt (env :port &quot;8080&quot;))
;;;                     :host (env :immutant-host &quot;127.0.0.1&quot;)}
;;;    :handler-config [routes
;;;                     handler/end-points-&gt;handlers
;;;                     handler/end-points-&gt;middlewares
;;;                     middleware]})
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Some of the configuration is static as `:db-uri`, some comes from environment variables like `:server-config`, fetched using the [environ](https://github.com/weavejester/environ) library.
;;; 
;;; We can take a look at our system contructor:
;; **

;; @@
(source system/make-system)
;; @@
;; ->
;;; (defn make-system [config]
;;;   (component/system-using (make-system-map config)
;;;                           (dependency-map)))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Its role is to build the different components of our system map passing it the config and declaring the dependencies between components with the `components/system-using` function.
;;; 
;;; Here is the code for the `make-system-map`
;; **

;; @@
(source system/make-system-map)
;; @@
;; ->
;;; (defn make-system-map [config]
;;;   (-&gt; system-map
;;;       (apply-config config)
;;;       (component/map-&gt;SystemMap )))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We can see here that we apply the config to a clojure map and then make a `component.SystemMap` out of it.
;;; 
;;; Here is the system map:
;; **

;; @@
(source system/system-map)
;; @@
;; ->
;;; (def system-map
;;;   {:db
;;;    #(datomic/make-database (:db-uri %))
;;; 
;;;    :schema-installer
;;;    #(datomic/make-schema-installer (:schema %))
;;; 
;;;    :web-request-handler
;;;    #(apply handler/make-handler (:handler-config %))
;;; 
;;;    :webserver
;;;    #(server/make-web-server (:server-config %))})
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; The keys allow us to name each component, the values are constructor functions that given the configuration will return a component.
;;; 
;;; We can also take a look at the dependencies between these components:
;; **

;; @@
(source system/dependency-map)
;; @@
;; ->
;;; (defn dependency-map []
;;;   {:schema-installer {:database :db}
;;;    :web-request-handler {:database :db}
;;;    :webserver {:handler-component :web-request-handler}})
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; [Component](https://github.com/stuartsierra/component)'s `SystemMap` implements the `Lifecycle` protocol in such a way that the starting order of components is determined by component dependencies. We can see for instance that the `:schema-installer` component depends on the `:db` component. Thus the `:db` will be started before the `:schema-installer` and once started the `:db` component will be `assoc`ed to the `:schema-installer` component before being started itself. When the system is stopped using the `component/stop` function, the components of the system are stop in the reverse of the starting order.
;; **

;; **
;;; 
;; **

;; **
;;; ## RinR's components
;;; 
;;; ### The datomic components
;;; The `reactrails-in-reagent.datomic` namespace contains the components we use to work with datomic.
;;; 
;;; - `DatomicDatabase` takes care of creating datomic connections
;;; - `DatomicSchemaInstaller` installs a schema into a datomic DB when started, it is meant to be use with the `DatomicDatabase` component.
;;; - `DatomicSeeder` is a component use in development only, it's role is to inject some development data in datomic.
;;; - `DatomicDeleter` does nothing when started and delete a datomic DB when stopped. We use this component in tests.
;;; 
;;; ### The server component
;;; The `reactrails-in-reagent.server` namespace contains the `WebServer` component. When started, this component will:
;;; 
;;; 1. create a websever with a ring handler recovered from a dependency
;;; 2. start the webserver.
;;; 
;;; ### The handler components
;;; In reactrail in reagent we have to components with the role of creating a ring handler, one in the namespace `reactrails-in-reagent.handler` the other in `dev.handler`.
;;; 
;;; #### The production handler.
;;; In `reactrails-in-reagent.handler` we find the handler component used in production. The fucntion starting this component is as follow:
;; **

;; @@
(source handler/start-handler)
;; @@
;; ->
;;; (defn start-handler [handler-component]
;;;   (println &quot;assembling handler&quot;)
;;;   (let [{:keys [routes-definition
;;;                 handlers
;;;                 middelware-associations
;;;                 general-middleware]} handler-component
;;; 
;;;         handler (compute-handler routes-definition
;;;                                  handlers
;;;                                  (middelware-associations handler-component))
;;;         handler&#x27; ((general-middleware handler-component) handler)]
;;;     (assoc handler-component
;;;       :handler handler&#x27;
;;;       :started? true)))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; The starting function recovers the values the handler component has been initialized with
;;; 
;;; - `routes-defintion`: a datastructure describing uri routes from which bidi can create a ring handler
;;; - `handlers`: a map specifying the mapping between the keywords naming routes in `routes-definition` and actual ring handlers. An example of mapping between route names to handlers can be found in the `reactrails-in-reagent.comment` namespace:
;; **

;; @@
(source comments/end-points->handlers)
;; @@
;; ->
;;; (def end-points-&gt;handlers
;;;   {:comments/comment-list comment-list
;;;    :comments/comment-entry comment-entry})
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; - `middleware-association`: a function returning a map specifying which particular middleware to apply to which handler in `handlers`. We can for instance find in the `reactrails-in-reagent.comment` namespace:
;; **

;; @@
(source comments/end-points->middlewares)
;; @@
;; ->
;;; (defn end-points-&gt;middlewares [handler-component]
;;;   {:comments/comment-list  (middleware-comment-list handler-component)
;;;    :comments/comment-entry (middleware-comment-entry handler-component)})
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Note that this middleware mapping comes from a function and isn't directly a static map. Some middleware will need state such as the database connection. This state is only available at runtime, not compile time. This is actually why the handler component in our system depend on the database component.
;; **

;; **
;;; - `general middleware`: the middleware from which all web requests must pass through regardless of the route.
;; **

;; **
;;; The `compute-handler` function's role is to:
;;; 
;;; 1. apply the route specific middleware to each route in `handlers`
;;; 2. inject these 'middlewared' handlers in our `routes-definition`
;;; 3. transform this definition into a ring handler with the bidi function `bidi.ring/make-handler`
;;; 
;;; We then apply the `general-middleware` to the computed handler and `assoc` it to our component.
;;; 
;; **

;; **
;;; #### The dev handler
;;; 
;;; The handler component found in the `dev.handler` namespace owes its existence to a trick clojure developpers use when working on a ring app in a REPL. The trick is to pass to the web server a clojure `Var`  pointing to the handler instead of the handler itself. This way when the `Var` pointing to the handler is redefined in the repl, the new definition is automatically used by the server, no need restart needed to see the change.
;;; 
;;; Since we are in a repl let's see how it works. Note that `#'` is a clojure reader macro for the special form `var` and so `#'a-var` is turned by the clojure reader into `(var a-var)`.
;;; 
;;; First we define a server as a function returning the result of a handler:
;; **

;; @@
(defn make-server [handler]
  (fn [request]
    (handler request)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;system/make-server</span>","value":"#'system/make-server"}
;; <=

;; **
;;; We define then a simple handler:
;; **

;; @@
(defn hello-handler [request]
  {:body "hello-world"})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;system/hello-handler</span>","value":"#'system/hello-handler"}
;; <=

;; **
;;; We can then 'start' instances of our server:
;; **

;; @@
(def server (make-server hello-handler))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;system/server</span>","value":"#'system/server"}
;; <=

;; @@
(def server-with-var (make-server #'hello-handler))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;system/server-with-var</span>","value":"#'system/server-with-var"}
;; <=

;; **
;;; In both cases we can call our servers:
;; **

;; @@
(server {:a :request})
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello-world&quot;</span>","value":"\"hello-world\""}],"value":"[:body \"hello-world\"]"}],"value":"{:body \"hello-world\"}"}
;; <=

;; @@
(server-with-var {:a :request})
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello-world&quot;</span>","value":"\"hello-world\""}],"value":"[:body \"hello-world\"]"}],"value":"{:body \"hello-world\"}"}
;; <=

;; **
;;; Both responses are the same, whether we use a the server started with the handler or the one started with the var pointing to the handler.
;;; 
;;; Now comes the part that differs, we realize that our handler doesn't provide a status in its reponse. We then redefine it.
;; **

;; @@
(defn hello-handler [request]
  {:status 200
   :body "hello world"})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;system/hello-handler</span>","value":"#'system/hello-handler"}
;; <=

;; **
;;; Our sever started with the handler directly doens't change:
;; **

;; @@
(server (:a :request))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello-world&quot;</span>","value":"\"hello-world\""}],"value":"[:body \"hello-world\"]"}],"value":"{:body \"hello-world\"}"}
;; <=

;; **
;;; It still uses the previous binding of the `hello-handler` var.
;;; 
;;; However the second server:
;; **

;; @@
(server-with-var (:a :request))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello world&quot;</span>","value":"\"hello world\""}],"value":"[:body \"hello world\"]"}],"value":"{:status 200, :body \"hello world\"}"}
;; <=

;; **
;;; uses the new binding of the `hello-server` var.
;;; 
;;; What happenned? 
;;; 
;;; - In the first case `(make-server hello-handler)`, `hello-handler` is resolved to the function bound to it, the function returned by `make-server` uses this value directly.
;; **

;; **
;;; - In the second case `(make-server #'hello-handler)`, the var is captured instead, unresolved.
;;; 
;;; 
;;; When we take a look at the code of `make-server` we see that the hanlder is used in the first position of a clojure function call. We have:
;; **

;; @@
(#'hello-handler {:a :request})
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello world&quot;</span>","value":"\"hello world\""}],"value":"[:body \"hello world\"]"}],"value":"{:status 200, :body \"hello world\"}"}
;; <=

;; **
;;; Clojure `Var`s actually implement the [IFN interface](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/IFn.java), which make them functions in their own right. When we take a look at the [implementation](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/Var.java#L362) we realize that `(#'hello-handler {:a :request})` is pretty much:
;; **

;; @@
(-> #'hello-handler
    deref
    (apply {:a :request}))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello world&quot;</span>","value":"\"hello world\""}],"value":"[:body \"hello world\"]"}],"value":"{:status 200, :body \"hello world\"}"}
;; <=

;; **
;;; Thus every time the second server is called, the mechanism of retrieving the value bounded to `#'hello-handler` is executed, yielding the latest binding.
;;; 
;;; We now add headers:
;; **

;; @@
(defn hello-handler [request]
  {:status 200
   :body "hello world"
   :headers {"Content-Type" "text/plain"}})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;system/hello-handler</span>","value":"#'system/hello-handler"}
;; <=

;; @@
(-> #'hello-handler
    deref
    (apply {:a :request}))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello world&quot;</span>","value":"\"hello world\""}],"value":"[:body \"hello world\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"}],"value":"{:status 200, :body \"hello world\", :headers {\"Content-Type\" \"text/plain\"}}"}
;; <=

;; @@
(#'hello-handler {:a :request})
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello world&quot;</span>","value":"\"hello world\""}],"value":"[:body \"hello world\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"}],"value":"{:status 200, :body \"hello world\", :headers {\"Content-Type\" \"text/plain\"}}"}
;; <=

;; @@
(server-with-var {:a :request})
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello world&quot;</span>","value":"\"hello world\""}],"value":"[:body \"hello world\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"}],"value":"{:status 200, :body \"hello world\", :headers {\"Content-Type\" \"text/plain\"}}"}
;; <=

;; **
;;; This allow us to work on the backend without having to restart our webserver, only reload the new code in the repl. 
;;; 
;;; In the app's the `dev.handler/DevHandler` does just that:
;; **

;; @@
(source dev.handler/handler-dev)
;; @@
;; ->
;;; (def handler-dev (-&gt; routes
;;;                      (routes/inject-handlers handler/end-points-&gt;handlers)
;;;                      (bidi.ring/make-handler)))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; and we start our `DevHandler` component using `#'handler-dev` this way:
;; **

;; @@
(source dev.handler/start-dev-handler)
;; @@
;; ->
;;; (defn start-dev-handler [component]
;;;   (println &quot;Starting DevHandler&quot;)
;;;   (let [middleware ((:general-middleware component) component)]
;;;     (assoc component
;;;       :started? true
;;;       :handler (middleware #&#x27;handler-dev))))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Last piece of the puzzle is the specific middleware for each route. In developpement we don't do the distinction between general and specific middelware and we use only one big general one:
;; **

;; @@
(source dev.system/middleware)
;; @@
;; ->
;;; (defn middleware [handler-component]
;;;   (comp #(wrap-resource % &quot;public&quot;)
;;;         wrap-stacktrace
;;;         wrap-reload
;;;         #(wrap-trace % :header :ui)
;;;         wrap-json-params
;;;         wrap-params
;;;         #(wrap-assoc-request % :conn (-&gt; handler-component :database :connection))))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; It is also worth noting that we use the `ring.middleware.reload.wrap-reload` middleware from the [ring-devel library](https://github.com/ring-clojure/ring/tree/master/ring-devel). This middleware automatically reloads the namespaces from edited files and the namespaces depending on them transitively. This way, when we save a file, we don't even need to reload code by hand in the REPL.
;; **

;; **
;;; ## The other systems
;;; 
;;; Since systems are just data we can replace part of our production system to suit the need of different execution context. 
;;; 
;;; In `reactrails-in-reeagent` we use two other systems than the one used in production:
;;; 
;;; - The namespace `dev.system` provides code that modifies the production system, replacing the handler component we use in production with the development one.
;;; - The `reactrails-in-reagent.system-test` provides code to make a system without the webserver component and with two handler components, the production one and the development one. This way we can test both.
;; **
