(ns testing.test-app
  (:require
    [compojure.core :refer :all]
    [com.zimpler.routedocs.core :refer :all]
    [ring.util.response :refer [response]]))

;; insert before first route macro
(reset-routedocs!)


(def root-path-message "\"These aren't the droids you're looking for.\"")

(def docs {:some-doc "This is some doc.
**Markdown** is supported."})


(defroutes app-routes

  (ANY+ "/" []
    (str \* root-path-message \*) ;; doc-string evaluated
    (response root-path-message))

  ;; If routedocs routes are used in a context, then you must use 'context+'
  (context+ "/api" []
    "All API routes start with `/api`"
    (POST+ "/endpoint" []
          "This is an endpoint"
          (response "Thank you"))
    (GET+ "/endpoint/:some-key" [some-key]
          nil  ;; Explicit nil result in route being listed, but no docstring
          (response "endpoint"))
    (GET+ "/list-stuff" []
          (:some-doc docs) ;; You may prefer
          (response "list of stuff here"))

    ;; This is a plain Compojure route, and so will not be included in the docs.
    (GET "/status" []
      (response "Status"))))


(def app
  (-> app-routes
      ;; Optionally serve docs at :path
      (wrap-routedocs :path "/docs")))


;; Insert after last route macro
(write-routedocs! "test_docs/routes.md")
