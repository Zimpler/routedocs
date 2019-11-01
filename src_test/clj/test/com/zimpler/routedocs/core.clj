(ns test.com.zimpler.routedocs.core
  (:require
    [clojure.test :refer :all]
    [clojure.string :as cs]
    [testing.test-app :as app]
    [ring.adapter.jetty :refer [run-jetty]]
    [clojure.java.io :as cio]))

(def test-server-port 60666)

(use-fixtures :once
              (fn [test-fn]
                (let [server (run-jetty app/app {:port test-server-port :join? false})]
                  (test-fn)
                  (.stop server))))

(deftest routedocs
  (testing "Middleware is is serving at ':path'."
    (let [res (slurp (str "http://Localhost:" test-server-port "/docs"))]
      (is (cs/includes? res app/root-path-message))))

  (testing "File is written."
    (is (.exists (cio/file "test_docs/routes.md")))

    (let [res (slurp "test_docs/routes.md")]
      (testing "File contains."
        (is (cs/includes? res app/root-path-message))
        (is (cs/includes? res (-> app/docs :some-doc cs/split-lines last)))
        (is (cs/includes? res "/api/list-stuff")))
      (testing "File does not contain."
        (is (not (cs/includes? res "/status")))))))
