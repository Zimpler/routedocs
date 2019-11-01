(ns com.zimpler.routedocs.core
    (:require
      [clojure.java.io :as cio]
      [clojure.pprint :refer [pprint]]
      [compojure.core :refer :all :as comp]
      [markdown
       [core :as md]
       [transformers :as mdt]
       [common :as mdc]]))

(defn- errln [& args]
  (binding [*out* *err*]
    (apply println args)))


(alter-meta! #'comp/context-route dissoc :private) ;; need access to this function


(def ^:private routes_ (atom :never-reset))
(def ^:private write-file_ (atom nil))


(defn- asserted_routes_ []
  (if (= @routes_ :never-reset)
    (errln "ERROR compiling routedocs. Did you include `(reset-routedocs!)` at beginning of your routes file?")
    routes_))


(defn- append-route! [route method doc form]
  (let [doc (if (string? doc)  doc (eval doc))
        context? (= method :CONTEXT)
        r        (-> (if context? route (nth route 2))
                     (assoc :method method :doc doc :contexts (-> form first meta :contexts)))]
    (swap! (asserted_routes_) conj r)
    (if context? r (concat (take 2 route) (list r) (drop 3 route)))))


(defn- get-contexts# [form]
       (:contexts (-> form first meta) []))


(defn- append-contexts# [contexts form]
  (let [sym (first form)]
    (cons
      (with-meta sym (assoc (meta sym)  :contexts (vec (concat (get-contexts# form) contexts))))
      (rest form))))


(defn- ^String no-path-info [s]
       (.replace s ":__path-info" ""))


(defn- ^String insert-md-quot [s]
       (.replace (str s) "\n" "  \n> "))

(defn- ^String good-newlines [s]
       (.replace (str s) "\n" "  \n"))

(defn- ^String build-docs* [routes]
  (let [sb (StringBuilder.)]
    (loop [routes routes]
      (if-let [{:keys [source keys method doc contexts] :as route} (first routes)]
        (do
          (.append sb (no-path-info (format "%s **%s%s**  %s \n" (name method) (apply str contexts) source keys)))
          ;(.append sb (if doc (format "> %s\n\n" (insert-md-quot doc)) "\n"))
          (.append sb (if doc (format "\n%s\n  \n" (good-newlines doc)) "\n"))
          (.append sb "- - - \n")
          (recur (rest routes)))
        (str sb)))))


(defn- ^String build-docs "Build text/markdown doc from 'routes_'" []
  (let [file @write-file_
        written-to (if file (format "Written to: `%s`  \n\n" file) "")
        routes     (build-docs* @(asserted_routes_))]
    (format "# Routedocs

%s## Routes

%s
" written-to routes)))


(defn- accept? [req pattern]
  (when-let [accept (get-in req [:headers "accept"])]
    (not (empty? (re-find pattern accept)))))


(defn- accept-html? [req]
       (accept? req #"text/html"))


(defmacro reset-routedocs!
  "Must be inserted at beginning of file containing main route macros.
  If not inserted, then an error message will be written to stderr,
  and an exception thrown for every call to one of the <route>+ macros."
  []
  (reset! routes_ [])
  (reset! write-file_ nil))


(defmacro GET+ [path args doc & body]
          (-> (compile-route :get path args body) (append-route! :GET doc &form)))

(defmacro POST+ [path args doc & body]
          (-> (compile-route :post path args body) (append-route! :POST doc &form)))

(defmacro PUT+ [path args doc & body]
          (-> (compile-route :put path args body) (append-route! :PUT doc &form)))

(defmacro DELETE+ [path args doc & body]
          (-> (compile-route :delete path args body) (append-route! :DELETE doc &form)))

(defmacro HEAD+ [path args doc & body]
          (-> (compile-route :head path args body) (append-route! :HEAD doc &form)))

(defmacro OPTIONS+ [path args doc & body]
          (-> (compile-route :options path args body) (append-route! :OPTIONS doc &form)))

(defmacro PATCH+ [path args doc & body]
          (-> (compile-route :patch path args body) (append-route! :PATCH doc &form)))

(defmacro ANY+ [path args doc & body]
          (-> (compile-route nil path args body) (append-route! :ANY doc &form)))

(defmacro context+ [path args doc & routes]
  (let [cr#       (append-route! (comp/context-route path) :CONTEXT doc &form)
        contexts# (conj (get-contexts# &form) path)
        routes#   (map (partial append-contexts# contexts#) routes)]
    `(make-context ~cr# (fn [request#]
                          (let-request [~args request#]
                                       (comp/routes ~@routes#))))))


(defmacro write-routedocs!
  "Writes your routedocs as Markdown to the specified file.
  'file' maybe a String or java.io.File.
  Must be inserted at end of file containing main route macros.
  (If not inserted, then no docs are written to file.)"
  [file]
  (try
    (.mkdirs (.getParentFile (cio/file file)))
    (spit file (build-docs))
    (reset! write-file_ file)
    (catch Exception e
      (errln "EXCEPTION at write-routedocs!" (.getMessage e)))))


(defn wrap-routedocs
  "Standard ring middleware which serves routedocs as Markdown or HTML at ':path'
  ':path' default to '/routedocs'
  Examples:  (wrap-routedocs)  (wrap-routedocs :path \"/some/path/for/docs\")"
  [handler & {:keys [path] :or {path "/routedocs"} :as ops}]
  (routes
    ;; add an additional route
    (GET path req
      (let [doc (build-docs)]
        (if (accept-html? req)
          (md/md-to-html-string doc :replacement-transformers (filter #(not= mdc/italics %) mdt/transformer-vector))
          doc)))
    handler))


