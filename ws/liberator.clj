;; gorilla-repl.fileformat = 1

;; **
;;; # Ring, Liberator and Bidi in RinR
;;; 
;;; In this worksheet we'll discuss the use of the [ring](https://github.com/ring-clojure), [bidi](https://github.com/juxt/bidi) and [liberator](http://clojure-liberator.github.io/liberator/) libraries in reactrails-in-reagent.
;; **

;; @@
(ns ring-bidi
  (:require 
    [ring.mock.request :as mock]
    [clojure.repl :refer [source]]
    [clojure.pprint :as pp]
    [ring.middleware.params :as ring-params]
    [bidi.bidi :as bidi]
    [bidi.ring]
    [com.rpl.specter :as specter]
    [liberator.core :refer [resource]]
    [reactrails-in-reagent.comment :as comments]
    [reactrails-in-reagent.routes :as routes]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## A little ring refresher
;;; When coding the server side of a web application in clojure, the standard way is to use [ring](https://github.com/ring-clojure). The basic idea behind [ring](https://github.com/ring-clojure) is that a http call is just a remote call of a function, the parammeters and response being pretty much hashmaps serialized in text form. 
;;; 
;;; Building our server boils down to coding a function, commonly called a ring handler, that takes a http request as a clojure map and returns a clojure map for response. [Ring](https://github.com/ring-clojure) provides us with the mean to use the function inside a http server via use of a ring adapter. The adapter also takes care of converting the http request from text to a clojure map and converting our clojure map response into an http text response.
;;; 
;;; Let's define some handlers to illustrate the use of [ring](https://github.com/ring-clojure).
;; **

;; **
;;; ### Basic handlers
;;; A hello world handler would be coded like this:
;; **

;; @@
(defn hello-handler [_]
  {:status 200
   :body "Hello world"
   :headers {"Content-Type" "text/plain"}})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/hello-handler</span>","value":"#'ring-bidi/hello-handler"}
;; <=

;; **
;;; Here we don't care about the request (parameter `_`). We just return a response map with the body "hello world".
;; **

;; @@
(hello-handler "whatever the http request is")
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;Hello world&quot;</span>","value":"\"Hello world\""}],"value":"[:body \"Hello world\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"}],"value":"{:status 200, :body \"Hello world\", :headers {\"Content-Type\" \"text/plain\"}}"}
;; <=

;; **
;;; A echo server might be coded like this:
;; **

;; @@
(defn echo-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (str request)})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/echo-handler</span>","value":"#'ring-bidi/echo-handler"}
;; <=

;; **
;;; The [ring-mock](https://github.com/ring-clojure/ring-mock) library helping us in creating http request maps programatically, we could use our echo server as this:
;; **

;; @@
(def mock-request 
  (mock/request :get "index.html"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/mock-request</span>","value":"#'ring-bidi/mock-request"}
;; <=

;; @@
mock-request
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:server-port</span>","value":":server-port"},{"type":"html","content":"<span class='clj-long'>80</span>","value":"80"}],"value":"[:server-port 80]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:server-name</span>","value":":server-name"},{"type":"html","content":"<span class='clj-string'>&quot;localhost&quot;</span>","value":"\"localhost\""}],"value":"[:server-name \"localhost\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:remote-addr</span>","value":":remote-addr"},{"type":"html","content":"<span class='clj-string'>&quot;localhost&quot;</span>","value":"\"localhost\""}],"value":"[:remote-addr \"localhost\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:uri</span>","value":":uri"},{"type":"html","content":"<span class='clj-string'>&quot;index.html&quot;</span>","value":"\"index.html\""}],"value":"[:uri \"index.html\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:query-string</span>","value":":query-string"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}],"value":"[:query-string nil]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheme</span>","value":":scheme"},{"type":"html","content":"<span class='clj-keyword'>:http</span>","value":":http"}],"value":"[:scheme :http]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:request-method</span>","value":":request-method"},{"type":"html","content":"<span class='clj-keyword'>:get</span>","value":":get"}],"value":"[:request-method :get]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;host&quot;</span>","value":"\"host\""},{"type":"html","content":"<span class='clj-string'>&quot;localhost&quot;</span>","value":"\"localhost\""}],"value":"[\"host\" \"localhost\"]"}],"value":"{\"host\" \"localhost\"}"}],"value":"[:headers {\"host\" \"localhost\"}]"}],"value":"{:server-port 80, :server-name \"localhost\", :remote-addr \"localhost\", :uri \"index.html\", :query-string nil, :scheme :http, :request-method :get, :headers {\"host\" \"localhost\"}}"}
;; <=

;; @@
(echo-handler mock-request)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;{:server-port 80, :server-name \\&quot;localhost\\&quot;, :remote-addr \\&quot;localhost\\&quot;, :uri \\&quot;index.html\\&quot;, :query-string nil, :scheme :http, :request-method :get, :headers {\\&quot;host\\&quot; \\&quot;localhost\\&quot;}}&quot;</span>","value":"\"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\""}],"value":"[:body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"]"}],"value":"{:status 200, :headers {\"Content-Type\" \"text/plain\"}, :body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"}"}
;; <=

;; **
;;; Note that we can test our web app without ever starting a web server, we can just create mock requests and pass them to our handlers to see the results of a http call.
;; **

;; **
;;; ### Ring Middleware
;;; With ring comes the notion of middlewares. Middlewares are functions that encapsulate functionnality that we want to reuse on multiple handlers. These middlewares are higher order function that take a ring handler and return a new ring handler.
;;; 
;;; In the case of our `echo-handler` we might want to decouple the coercion of the response body into a string by using a middleware.
;; **

;; @@
(defn new-echo-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body request})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/new-echo-handler</span>","value":"#'ring-bidi/new-echo-handler"}
;; <=

;; @@
(new-echo-handler mock-request)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:server-port</span>","value":":server-port"},{"type":"html","content":"<span class='clj-long'>80</span>","value":"80"}],"value":"[:server-port 80]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:server-name</span>","value":":server-name"},{"type":"html","content":"<span class='clj-string'>&quot;localhost&quot;</span>","value":"\"localhost\""}],"value":"[:server-name \"localhost\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:remote-addr</span>","value":":remote-addr"},{"type":"html","content":"<span class='clj-string'>&quot;localhost&quot;</span>","value":"\"localhost\""}],"value":"[:remote-addr \"localhost\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:uri</span>","value":":uri"},{"type":"html","content":"<span class='clj-string'>&quot;index.html&quot;</span>","value":"\"index.html\""}],"value":"[:uri \"index.html\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:query-string</span>","value":":query-string"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}],"value":"[:query-string nil]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheme</span>","value":":scheme"},{"type":"html","content":"<span class='clj-keyword'>:http</span>","value":":http"}],"value":"[:scheme :http]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:request-method</span>","value":":request-method"},{"type":"html","content":"<span class='clj-keyword'>:get</span>","value":":get"}],"value":"[:request-method :get]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;host&quot;</span>","value":"\"host\""},{"type":"html","content":"<span class='clj-string'>&quot;localhost&quot;</span>","value":"\"localhost\""}],"value":"[\"host\" \"localhost\"]"}],"value":"{\"host\" \"localhost\"}"}],"value":"[:headers {\"host\" \"localhost\"}]"}],"value":"{:server-port 80, :server-name \"localhost\", :remote-addr \"localhost\", :uri \"index.html\", :query-string nil, :scheme :http, :request-method :get, :headers {\"host\" \"localhost\"}}"}],"value":"[:body {:server-port 80, :server-name \"localhost\", :remote-addr \"localhost\", :uri \"index.html\", :query-string nil, :scheme :http, :request-method :get, :headers {\"host\" \"localhost\"}}]"}],"value":"{:status 200, :headers {\"Content-Type\" \"text/plain\"}, :body {:server-port 80, :server-name \"localhost\", :remote-addr \"localhost\", :uri \"index.html\", :query-string nil, :scheme :http, :request-method :get, :headers {\"host\" \"localhost\"}}}"}
;; <=

;; **
;;; Our new handler doesn't take the reponsibility to convert the body of the response into a string. 
;;; 
;;; We define our middleware like this:
;; **

;; @@
(defn wrap-str-response [handler]
  (fn [request]
    (update (handler request) :body str)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/wrap-str-response</span>","value":"#'ring-bidi/wrap-str-response"}
;; <=

;; **
;;; Now when we use our middleware
;; **

;; @@
(let [wrapped (wrap-str-response new-echo-handler)]
  (wrapped mock-request))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;{:server-port 80, :server-name \\&quot;localhost\\&quot;, :remote-addr \\&quot;localhost\\&quot;, :uri \\&quot;index.html\\&quot;, :query-string nil, :scheme :http, :request-method :get, :headers {\\&quot;host\\&quot; \\&quot;localhost\\&quot;}}&quot;</span>","value":"\"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\""}],"value":"[:body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"]"}],"value":"{:status 200, :headers {\"Content-Type\" \"text/plain\"}, :body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"}"}
;; <=

;; **
;;; we get a string in the body of the response.
;;; 
;;; [Ring](https://github.com/ring-clojure) already provides some middleware such as the ones in `ring.middleware.params`.
;; **

;; @@
(def a-request-with-params 
  (mock/request :get "/a-route" {:param1 1 :param2 2}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/a-request-with-params</span>","value":"#'ring-bidi/a-request-with-params"}
;; <=

;; @@
(-> a-request-with-params
    (new-echo-handler )
    :body
    (select-keys [:query-string :query-params :params]))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:query-string</span>","value":":query-string"},{"type":"html","content":"<span class='clj-string'>&quot;param1=1&amp;param2=2&quot;</span>","value":"\"param1=1&param2=2\""}],"value":"[:query-string \"param1=1&param2=2\"]"}],"value":"{:query-string \"param1=1&param2=2\"}"}
;; <=

;; @@
(-> a-request-with-params
    (as-> request 
          ((ring-params/wrap-params new-echo-handler) request))
    :body
    (select-keys [:query-string :query-params :params]))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:query-string</span>","value":":query-string"},{"type":"html","content":"<span class='clj-string'>&quot;param1=1&amp;param2=2&quot;</span>","value":"\"param1=1&param2=2\""}],"value":"[:query-string \"param1=1&param2=2\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:query-params</span>","value":":query-params"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;param1&quot;</span>","value":"\"param1\""},{"type":"html","content":"<span class='clj-string'>&quot;1&quot;</span>","value":"\"1\""}],"value":"[\"param1\" \"1\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;param2&quot;</span>","value":"\"param2\""},{"type":"html","content":"<span class='clj-string'>&quot;2&quot;</span>","value":"\"2\""}],"value":"[\"param2\" \"2\"]"}],"value":"{\"param1\" \"1\", \"param2\" \"2\"}"}],"value":"[:query-params {\"param1\" \"1\", \"param2\" \"2\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:params</span>","value":":params"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;param1&quot;</span>","value":"\"param1\""},{"type":"html","content":"<span class='clj-string'>&quot;1&quot;</span>","value":"\"1\""}],"value":"[\"param1\" \"1\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;param2&quot;</span>","value":"\"param2\""},{"type":"html","content":"<span class='clj-string'>&quot;2&quot;</span>","value":"\"2\""}],"value":"[\"param2\" \"2\"]"}],"value":"{\"param1\" \"1\", \"param2\" \"2\"}"}],"value":"[:params {\"param1\" \"1\", \"param2\" \"2\"}]"}],"value":"{:query-string \"param1=1&param2=2\", :query-params {\"param1\" \"1\", \"param2\" \"2\"}, :params {\"param1\" \"1\", \"param2\" \"2\"}}"}
;; <=

;; **
;;; For more documentation see the [ring website](https://github.com/ring-clojure)
;; **

;; **
;;; ## A bibi intro
;;; [Juxt's bidi](https://github.com/juxt/bidi) is a library that gives us tools to define http routes as data and matching uris against these definitions.
;;; 
;;; What if we want our http service to provide both the `hello world` and the `echo` functionality?
;;; 
;;; We start defining our routes:
;;; 
;; **

;; @@
(def routes ["" [["/hello" :hello-handler]
                 ["/echo" :echo-handler]]])
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/routes</span>","value":"#'ring-bidi/routes"}
;; <=

;; **
;;; We can match against these routes like this:
;; **

;; @@
(bidi/match-route routes "/hello")
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:handler</span>","value":":handler"},{"type":"html","content":"<span class='clj-keyword'>:hello-handler</span>","value":":hello-handler"}],"value":"[:handler :hello-handler]"}],"value":"{:handler :hello-handler}"}
;; <=

;; **
;;; We can now define our handler this way:
;; **

;; @@
(defn hello-echo-handler [request]
  (if-let [match (bidi/match-route routes (:uri request))]
    (case (:handler match)
      :hello-handler (hello-handler request)
      :echo-handler (echo-handler request))
    {:status 404}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/hello-echo-handler</span>","value":"#'ring-bidi/hello-echo-handler"}
;; <=

;; **
;;; and test it:
;; **

;; @@
(hello-echo-handler (mock/request :get "/hello"))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;Hello world&quot;</span>","value":"\"Hello world\""}],"value":"[:body \"Hello world\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"}],"value":"{:status 200, :body \"Hello world\", :headers {\"Content-Type\" \"text/plain\"}}"}
;; <=

;; @@
(hello-echo-handler (mock/request :get "/echo"))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;{:server-port 80, :server-name \\&quot;localhost\\&quot;, :remote-addr \\&quot;localhost\\&quot;, :uri \\&quot;/echo\\&quot;, :query-string nil, :scheme :http, :request-method :get, :headers {\\&quot;host\\&quot; \\&quot;localhost\\&quot;}}&quot;</span>","value":"\"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"/echo\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\""}],"value":"[:body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"/echo\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"]"}],"value":"{:status 200, :headers {\"Content-Type\" \"text/plain\"}, :body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"/echo\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"}"}
;; <=

;; **
;;; Voilà! We have a ring handler providing both the functionalities we wanted.
;;; 
;;; However, as soon as the app grows, handling the route matching and handler dispatching as in `hello-echo-hendler` can become tedious. [Bidi](https://github.com/juxt/bidi) provides a solution to automaticaly generate a handler that takes care of this logic for us.
;;; 
;;; First we need to replace the keywords `:hello-handler` and `:echo-handler` in our routes definition by the actual handler functions `hello-handler` and `echo-handler`. Second we use the `bidi.ring/make-handler` function to generate the handler.
;;; 
;;; To do so we define a mapping:
;; **

;; @@
(def endpoint->symbol
  {:hello-handler 'hello-handler
   :echo-handler  'echo-handler})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/endpoint-&gt;symbol</span>","value":"#'ring-bidi/endpoint->symbol"}
;; <=

;; **
;;; Then using the specter library we can walk the routes datastructure and replace keywords with handlers where appropriate.
;; **

;; @@
(defn inject-handlers [routes mapping]
  (specter/transform (specter/walker keyword?)
                     (fn [kw] (get mapping kw kw))
                     routes))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/inject-handlers</span>","value":"#'ring-bidi/inject-handlers"}
;; <=

;; @@
(inject-handlers routes endpoint->symbol)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;&quot;</span>","value":"\"\""},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;/hello&quot;</span>","value":"\"/hello\""},{"type":"html","content":"<span class='clj-symbol'>hello-handler</span>","value":"hello-handler"}],"value":"[\"/hello\" hello-handler]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;/echo&quot;</span>","value":"\"/echo\""},{"type":"html","content":"<span class='clj-symbol'>echo-handler</span>","value":"echo-handler"}],"value":"[\"/echo\" echo-handler]"}],"value":"[[\"/hello\" hello-handler] [\"/echo\" echo-handler]]"}],"value":"[\"\" [[\"/hello\" hello-handler] [\"/echo\" echo-handler]]]"}
;; <=

;; **
;;; We can see that we replaced the keywords with their associated symbols in the routes data. Let's break down what happend here.
;;; 
;;; We can see what parts of the routes data specter will try to transform
;;; using the `specter/select` function:
;; **

;; @@
(specter/select (specter/walker keyword?) routes)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:hello-handler</span>","value":":hello-handler"},{"type":"html","content":"<span class='clj-keyword'>:echo-handler</span>","value":":echo-handler"}],"value":"(:hello-handler :echo-handler)"}
;; <=

;; **
;;; For each one of these values specter will apply the tranformation function
;;; ```clojure
;;; (fn [kw] (get mapping kw kw))
;;; ```
;;; that we can test
;; **

;; @@
(let [mapping endpoint->symbol]
  ((fn [kw] (get mapping kw kw)) :hello-handler))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-symbol'>hello-handler</span>","value":"hello-handler"}
;; <=

;; @@
(let [mapping endpoint->symbol]
  ((fn [kw] (get mapping kw kw)) :handler-without-mapping))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-keyword'>:handler-without-mapping</span>","value":":handler-without-mapping"}
;; <=

;; **
;;; Using now a mapping from keywords to actual handlers:
;; **

;; @@
(def endpoint->handler
  {:hello-handler hello-handler
   :echo-handler  echo-handler})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/endpoint-&gt;handler</span>","value":"#'ring-bidi/endpoint->handler"}
;; <=

;; @@
(inject-handlers routes endpoint->handler)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;&quot;</span>","value":"\"\""},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;/hello&quot;</span>","value":"\"/hello\""},{"type":"html","content":"<span class='clj-unkown'>#object[ring_bidi$hello_handler 0x4eeab918 &quot;ring_bidi$hello_handler@4eeab918&quot;]</span>","value":"#object[ring_bidi$hello_handler 0x4eeab918 \"ring_bidi$hello_handler@4eeab918\"]"}],"value":"[\"/hello\" #object[ring_bidi$hello_handler 0x4eeab918 \"ring_bidi$hello_handler@4eeab918\"]]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;/echo&quot;</span>","value":"\"/echo\""},{"type":"html","content":"<span class='clj-unkown'>#object[ring_bidi$echo_handler 0x56f517c6 &quot;ring_bidi$echo_handler@56f517c6&quot;]</span>","value":"#object[ring_bidi$echo_handler 0x56f517c6 \"ring_bidi$echo_handler@56f517c6\"]"}],"value":"[\"/echo\" #object[ring_bidi$echo_handler 0x56f517c6 \"ring_bidi$echo_handler@56f517c6\"]]"}],"value":"[[\"/hello\" #object[ring_bidi$hello_handler 0x4eeab918 \"ring_bidi$hello_handler@4eeab918\"]] [\"/echo\" #object[ring_bidi$echo_handler 0x56f517c6 \"ring_bidi$echo_handler@56f517c6\"]]]"}],"value":"[\"\" [[\"/hello\" #object[ring_bidi$hello_handler 0x4eeab918 \"ring_bidi$hello_handler@4eeab918\"]] [\"/echo\" #object[ring_bidi$echo_handler 0x56f517c6 \"ring_bidi$echo_handler@56f517c6\"]]]]"}
;; <=

;; **
;;; we can define our new app handler:
;; **

;; @@
(def hello-echo-final-handler
  (-> routes
      (inject-handlers endpoint->handler)
      (bidi.ring/make-handler)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/hello-echo-final-handler</span>","value":"#'ring-bidi/hello-echo-final-handler"}
;; <=

;; @@
(hello-echo-final-handler (mock/request :get "/hello"))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;Hello world&quot;</span>","value":"\"Hello world\""}],"value":"[:body \"Hello world\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"}],"value":"{:status 200, :body \"Hello world\", :headers {\"Content-Type\" \"text/plain\"}}"}
;; <=

;; @@
(hello-echo-final-handler (mock/request :get "/echo"))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain&quot;</span>","value":"\"text/plain\""}],"value":"[\"Content-Type\" \"text/plain\"]"}],"value":"{\"Content-Type\" \"text/plain\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;{:remote-addr \\&quot;localhost\\&quot;, :params nil, :route-params nil, :headers {\\&quot;host\\&quot; \\&quot;localhost\\&quot;}, :server-port 80, :uri \\&quot;/echo\\&quot;, :server-name \\&quot;localhost\\&quot;, :query-string nil, :scheme :http, :request-method :get}&quot;</span>","value":"\"{:remote-addr \\\"localhost\\\", :params nil, :route-params nil, :headers {\\\"host\\\" \\\"localhost\\\"}, :server-port 80, :uri \\\"/echo\\\", :server-name \\\"localhost\\\", :query-string nil, :scheme :http, :request-method :get}\""}],"value":"[:body \"{:remote-addr \\\"localhost\\\", :params nil, :route-params nil, :headers {\\\"host\\\" \\\"localhost\\\"}, :server-port 80, :uri \\\"/echo\\\", :server-name \\\"localhost\\\", :query-string nil, :scheme :http, :request-method :get}\"]"}],"value":"{:status 200, :headers {\"Content-Type\" \"text/plain\"}, :body \"{:remote-addr \\\"localhost\\\", :params nil, :route-params nil, :headers {\\\"host\\\" \\\"localhost\\\"}, :server-port 80, :uri \\\"/echo\\\", :server-name \\\"localhost\\\", :query-string nil, :scheme :http, :request-method :get}\"}"}
;; <=

;; **
;;; No need to handle routes matching and dispatching by hand.
;; **

;; **
;;; ## Liberator
;;; 
;;; In `reactrails-in-reagent` we use [liberator](http://clojure-liberator.github.io/liberator/) to generate handlers for the differents http resources the app serves. This library helps us define these resources in a clean manners taking care for us of the intricacies that come with building REST services.
;;; 
;;; We can define our `hello-handler` as a http resources using [liberator](http://clojure-liberator.github.io/liberator/) `resource` function:
;; **

;; @@
(def hello-resource 
  (resource {:handle-ok (fn [_] "hello world")}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/hello-resource</span>","value":"#'ring-bidi/hello-resource"}
;; <=

;; **
;;; A liberator resource is a ring handler function computed from a specification described in a clojure map.
;; **

;; @@
(hello-resource mock-request)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain;charset=UTF-8&quot;</span>","value":"\"text/plain;charset=UTF-8\""}],"value":"[\"Content-Type\" \"text/plain;charset=UTF-8\"]"}],"value":"{\"Content-Type\" \"text/plain;charset=UTF-8\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain;charset=UTF-8\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;hello world&quot;</span>","value":"\"hello world\""}],"value":"[:body \"hello world\"]"}],"value":"{:status 200, :headers {\"Content-Type\" \"text/plain;charset=UTF-8\"}, :body \"hello world\"}"}
;; <=

;; **
;;; We can can also define our `echo-handler`:
;; **

;; @@
(def echo-resource 
  (resource {:handle-ok (fn [context]
                          (str (:request context)))}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/echo-resource</span>","value":"#'ring-bidi/echo-resource"}
;; <=

;; @@
(echo-resource mock-request)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain;charset=UTF-8&quot;</span>","value":"\"text/plain;charset=UTF-8\""}],"value":"[\"Content-Type\" \"text/plain;charset=UTF-8\"]"}],"value":"{\"Content-Type\" \"text/plain;charset=UTF-8\"}"}],"value":"[:headers {\"Content-Type\" \"text/plain;charset=UTF-8\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;{:server-port 80, :server-name \\&quot;localhost\\&quot;, :remote-addr \\&quot;localhost\\&quot;, :uri \\&quot;index.html\\&quot;, :query-string nil, :scheme :http, :request-method :get, :headers {\\&quot;host\\&quot; \\&quot;localhost\\&quot;}}&quot;</span>","value":"\"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\""}],"value":"[:body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"]"}],"value":"{:status 200, :headers {\"Content-Type\" \"text/plain;charset=UTF-8\"}, :body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"}"}
;; <=

;; **
;;; Note here that [liberator](http://clojure-liberator.github.io/liberator/) passes a context map that contains the request to our handler and not the request itself. 
;;; 
;;; Also [liberator](http://clojure-liberator.github.io/liberator/) tries to coerce the body of a response in a MIME type. We can then let [liberator](http://clojure-liberator.github.io/liberator/) coerce the response by itself:
;; **

;; @@
(def echo-resoucre' 
  (resource {:available-media-types ["application/edn"]
             :handle-ok (fn [context] (:request context))}))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;ring-bidi/echo-resoucre&#x27;</span>","value":"#'ring-bidi/echo-resoucre'"}
;; <=

;; @@
(echo-resoucre' mock-request)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>200</span>","value":"200"}],"value":"[:status 200]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;application/edn;charset=UTF-8&quot;</span>","value":"\"application/edn;charset=UTF-8\""}],"value":"[\"Content-Type\" \"application/edn;charset=UTF-8\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Vary&quot;</span>","value":"\"Vary\""},{"type":"html","content":"<span class='clj-string'>&quot;Accept&quot;</span>","value":"\"Accept\""}],"value":"[\"Vary\" \"Accept\"]"}],"value":"{\"Content-Type\" \"application/edn;charset=UTF-8\", \"Vary\" \"Accept\"}"}],"value":"[:headers {\"Content-Type\" \"application/edn;charset=UTF-8\", \"Vary\" \"Accept\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;{:server-port 80, :server-name \\&quot;localhost\\&quot;, :remote-addr \\&quot;localhost\\&quot;, :uri \\&quot;index.html\\&quot;, :query-string nil, :scheme :http, :request-method :get, :headers {\\&quot;host\\&quot; \\&quot;localhost\\&quot;}}&quot;</span>","value":"\"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\""}],"value":"[:body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"]"}],"value":"{:status 200, :headers {\"Content-Type\" \"application/edn;charset=UTF-8\", \"Vary\" \"Accept\"}, :body \"{:server-port 80, :server-name \\\"localhost\\\", :remote-addr \\\"localhost\\\", :uri \\\"index.html\\\", :query-string nil, :scheme :http, :request-method :get, :headers {\\\"host\\\" \\\"localhost\\\"}}\"}"}
;; <=

;; **
;;; More on the representation coercion can be found [here](http://clojure-liberator.github.io/liberator/doc/representations.html).
;;; 
;;; The power of [liberator](http://clojure-liberator.github.io/liberator/) comes from the fact that it implements a lot of decisions that the http spec expects of us. We can then hook into these decisions by growing our resource as we need.
;;; 
;;; For instance take a look at:
;; **

;; @@
(hello-resource (mock/request :post "/"))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:headers</span>","value":":headers"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Allow&quot;</span>","value":"\"Allow\""},{"type":"html","content":"<span class='clj-string'>&quot;GET, HEAD&quot;</span>","value":"\"GET, HEAD\""}],"value":"[\"Allow\" \"GET, HEAD\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Content-Type&quot;</span>","value":"\"Content-Type\""},{"type":"html","content":"<span class='clj-string'>&quot;text/plain;charset=UTF-8&quot;</span>","value":"\"text/plain;charset=UTF-8\""}],"value":"[\"Content-Type\" \"text/plain;charset=UTF-8\"]"}],"value":"{\"Allow\" \"GET, HEAD\", \"Content-Type\" \"text/plain;charset=UTF-8\"}"}],"value":"[:headers {\"Allow\" \"GET, HEAD\", \"Content-Type\" \"text/plain;charset=UTF-8\"}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:status</span>","value":":status"},{"type":"html","content":"<span class='clj-long'>405</span>","value":"405"}],"value":"[:status 405]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:body</span>","value":":body"},{"type":"html","content":"<span class='clj-string'>&quot;Method not allowed.&quot;</span>","value":"\"Method not allowed.\""}],"value":"[:body \"Method not allowed.\"]"}],"value":"{:headers {\"Allow\" \"GET, HEAD\", \"Content-Type\" \"text/plain;charset=UTF-8\"}, :status 405, :body \"Method not allowed.\"}"}
;; <=

;; **
;;; Since we haven't specified what HTTP methods are allowed on our `hello-resource` [liberator](http://clojure-liberator.github.io/liberator/) allows only GET and HEAD. As we can see, tring to POST results in a `405 Method not allowed` response. For more information on the decisions in which we can hook see [this page](http://clojure-liberator.github.io/liberator/doc/decisions.html). To see in what order decisions are made see [the decision graph page](http://clojure-liberator.github.io/liberator/tutorial/decision-graph.html).
;; **

;; **
;;; ## Usage in RinR
;;; 
;;; In `reactrails-in-reagent` our http resources are declared using [liberator](http://clojure-liberator.github.io/liberator/). [Bidi](https://github.com/juxt/bidi) is used for routing and we use the same injection technique of handlers into the routes data shown previoulsly.
;;; 
;;; ### The resources
;;; The http resources used in our app are defined in the `reactrails-in-reagent.comment` namespace.
;;; 
;;; #### comment-entry
;;; The `comment-entry` resource is defined a follow:
;; **

;; @@
(source comments/comment-entry)
;; @@
;; ->
;;; (def comment-entry
;;;   (resource {:available-media-types [&quot;application/json&quot;]
;;;              :malformed?            malformed-comment-entry-params?
;;;              :exists?               comment-entry-exists?
;;;              :handle-ok             response-comment-entry}))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; This his the resource returned when calling a uri similar to:
;; **

;; @@
(routes/path-for :comments/comment-entry :id 123456)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;/comments/123456&quot;</span>","value":"\"/comments/123456\""}
;; <=

;; **
;;; Let's examine our resource:
;;; 
;;; - `:available-media-types ["application/json"]` we handle content negociation here specifying that our resource returns JSON data only
;;; - `:malformed? malformed-comment-entry-params?` here we tell [liberator](http://clojure-liberator.github.io/liberator/) to check if the parameters of the http request are acceptable for the resource. The checking is handled by the function `malformed-comment-entry-params?` and if this function returns true liberator will return a `400 malformed` http responce. 
;;; - `:exists? comment-entry-exists?` We now know that the request is well formed, but it doesn't mean that the requested comment exists. We tell liberator to check just for that with the function `comment-entry-exist`. If not existant liberator will return a `404 not found` response.
;;; - `:handle-ok response-comment-entry` finally our handler that will return the comment.
;;; 
;;; There is a subtlety to notice in the function `malformed-comment-entry-params?`:
;; **

;; @@
(source comments/malformed-comment-entry-params?)
;; @@
;; ->
;;; (defn malformed-comment-entry-params? [ctx]
;;;   (let [request (:request ctx)
;;;         params (:route-params request)
;;;         checked-params (comment-entry-params-coercer params)]
;;;     (if-not (s-utils/error? checked-params)
;;;       [false {:checked-params checked-params}]
;;;       true)))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; In this function we try to coerce the id parameter into a long with the `comment-entry-params-coercer`:
;; **

;; @@
(source comments/comment-entry-params-coercer)
;; @@
;; ->
;;; (def comment-entry-params-coercer
;;;   (coerce/coercer comment-entry-params-schema
;;;                   {Long long-coercion}))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; If the coercion fails we return true to malformed. If it succeeds we however returns a vector containing 
;;; false and a map containing the coercion. This tell liberator that we find that the request is not malformed and that we want it to merge our `{:checked-params checked-params}` map to the context. 
;;; 
;;; This way every other decision down the line will have access to a context that has the coerced parameters.
;;; The `comment-entry-exists?` will use this data to try to get the comment from our datomic database:
;; **

;; @@
(source comments/comment-entry-exists?)
;; @@
;; ->
;;; (defn comment-entry-exists? [ctx]
;;;   (let [conn (get-in  ctx [:request :conn])
;;;         {id :id} (:checked-params ctx)
;;;         comment (get-comment conn id)]
;;;     (if comment
;;;       [true {::comment comment}]
;;;       false)))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We can see here that the id used to fetch the comment from datomic comes from the `:checked-params` of the context `ctx`. We can also see that we reuse this pattern to assoc the comment to the context in the case we actually found it in the database. 
;;; 
;;; This is why `response-comment-entry` just returns the comment found in the context.
;; **

;; @@
(source comments/response-comment-entry)
;; @@
;; ->
;;; (defn response-comment-entry [ctx]
;;;   (::comment ctx))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; If we didn't use this functionality of [liberator](http://clojure-liberator.github.io/liberator/), we would have had to fetch the comment twice (in `:exists?` and `:handle-ok`) and do the coercion thrice (in `:malformed?`, `:exists?` and `:handle-ok`). More information can be found in the [execution model page](http://clojure-liberator.github.io/liberator/doc/execution-model.html).
;; **

;; **
;;; #### comment-list
;;; Our `comment-list` resource is defined similarly to the previous one:
;; **

;; @@
(source comments/comment-list)
;; @@
;; ->
;;; (def comment-list
;;;   (resource {:available-media-types [&quot;application/json&quot;]
;;;              :allowed-methods [:post :get :head]
;;;              :malformed? malformed-comment-list-params?
;;;              :handle-ok response-comment-list
;;;              :post! post-comment!
;;;              :post-redirect? post-redirection}))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We arrive at that resource with the uri:
;; **

;; @@
(routes/path-for :comments/comment-list)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-string'>&quot;/comments&quot;</span>","value":"\"/comments\""}
;; <=

;; **
;;; A bit more of the [liberator](http://clojure-liberator.github.io/liberator/) decisions are used in this resource. POST requests being allowed with `:allowed-methods [:post :get :head]`, the side effects of such a request are defined with `:post! post-comment!`.
;;; 
;; **

;; @@
(source comments/post-comment!)
;; @@
;; ->
;;; (defn post-comment! [ctx]
;;;   (let [comment (::comment ctx)
;;;         conn (-&gt; ctx :request :conn)]
;;;     {::id (transact-new-comment conn comment)}))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We also define `:post-redirect? post-redirection` to redirect to the `comment-entry` resource after posting.
;; **

;; @@
(source comments/post-redirection)
;; @@
;; ->
;;; (defn post-redirection [ctx]
;;;   (let [id (::id ctx)
;;;         location (routes/path-for :comments/comment-entry :id id)]
;;;     {:location location}))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### Routing and middleware
;;; 
;;; After the resource defintions we can find this code in the `reactrails-in-reagent/comment` namespace:
;; **

;; @@
(source comments/end-points->handlers)
(println)
(source comments/end-points->middlewares)
;; @@
;; ->
;;; (def end-points-&gt;handlers
;;;   {:comments/comment-list comment-list
;;;    :comments/comment-entry comment-entry})
;;; 
;;; (defn end-points-&gt;middlewares [handler-component]
;;;   {:comments/comment-list  (middleware-comment-list handler-component)
;;;    :comments/comment-entry (middleware-comment-entry handler-component)})
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; These definitions provides mappings corresponding to the routes of the application in `reactrails-in-reagent.routes`:
;; **

;; @@
(source routes/index-route)
(println)

(source routes/missed-route)
(println)

(source routes/comments-routes)
(println)

(source routes/routes)
(println "\n;; And the routes data\n")

(pp/pprint routes/routes)
;; @@
;; ->
;;; (def index-route [(bidi/alts &quot;&quot; &quot;/&quot;) :index])
;;; 
;;; (def missed-route [true :miss-404])
;;; 
;;; (def comments-routes [&quot;/comments&quot; {(bidi/alts &quot;&quot; &quot;/&quot;) :comments/comment-list
;;;                                    [&quot;/&quot; :id]          :comments/comment-entry}])
;;; 
;;; (def routes [&quot;&quot; [index-route
;;;                  comments-routes
;;;                  missed-route]])
;;; 
;;; ;; And the routes data
;;; 
;;; [&quot;&quot;
;;;  [[{:alts (&quot;&quot; &quot;/&quot;)} :index]
;;;   [&quot;/comments&quot;
;;;    {{:alts (&quot;&quot; &quot;/&quot;)} :comments/comment-list,
;;;     [&quot;/&quot; :id] :comments/comment-entry}]
;;;   [true :miss-404]]]
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; As with the previous bidi examples, the routing is described in a clojure data structure passed to bidi to 
;;; generate the ring handler used in the application. In this datastructure, each endpoint is represented with a keyword. The `end-points->handlers` value expresses where to inject the resources in our routing data and `end-points->middlewares` allows us to declare which middleware should be used on which resource.
;;; 
;;; We also have the injection function similar to the one we defined in the previous examples:
;; **

;; @@
(source routes/inject-handlers)
;; @@
;; ->
;;; (defn inject-handlers [routes endpoints-&gt;handler]
;;;   &quot;Walks the `routes` datastructure and replaces endpoints names with
;;;   the actual handlers for the endpoints as defined in the mapping `endpoints-&gt;handler`.&quot;
;;;   (s/transform endpoints-path
;;;                (fn [v] (get endpoints-&gt;handler v v))
;;;                routes))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; The actual assembling of the routes, resources, other handlers and middelware is done in the `reactrails-in-reagent.handler` namespace. The process is done inside a component as in the [component library](https://github.com/stuartsierra/component). The details of this code are explained in another worksheet.
;; **

;; **
;;; ## Parting Notes
;;; The [ring](https://github.com/ring-clojure) library comes with a lot of [interesting](https://github.com/ring-clojure/ring/wiki) [documentation](http://ring-clojure.github.io/ring/). I also encourage to take a look at the Jetty adapter and particularly [how the ring handler is used](https://github.com/ring-clojure/ring/blob/1.4.0/ring-jetty-adapter/src/ring/adapter/jetty.clj#L20) in it.
;;; 
;;; The [yada](https://github.com/juxt/yada) library is an interesting new contender in the same space as [liberator](http://clojure-liberator.github.io/liberator/). In the newest releases [yada](https://github.com/juxt/yada)'s api seems to be similar to [liberator](http://clojure-liberator.github.io/liberator/)'s. It lets us define http resources as a clojure maps. We could even imagine possible to develop data transformations to convert [liberator](http://clojure-liberator.github.io/liberator/) resources into [yada](https://github.com/juxt/yada) ones. The library also seems to provide easy intragration with [swagger](http://swagger.io). It allows for full asynchronicity in the execution of the different decisions by using the [Aleph](http://aleph.io) server and the [manifold](https://github.com/ztellman/manifold) library which are fantastic technologies. [Yada](https://github.com/juxt/yada) is in my opinion, worth investigating as soon as its api stabilizes.
;;; 
;;; Middlewares are, I think, a really important concept to grasp when working with clojure. They are widely used in web development but not only. The more and more popular clojure build tool [Boot](http://boot-clj.com) relies on [middlewares to implement build pipelines](https://github.com/boot-clj/boot/wiki/Tasks). Finally [Clojure 1.7 transducers](http://clojure.org/reference/transducers) could be argued to be middlewares for reducing functions, emphasizing againg the need for a good understanding of the concept.
;; **
