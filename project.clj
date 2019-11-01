(defproject com.zimpler/routedocs "0.1.0"

  :description "Lightweight docs for Compojure routes"
  :url "https://github.com/Zimpler/routedocs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [compojure "1.6.1" :scope "provided"]
                 [markdown-clj "1.10.0"]]

  :plugins      [[lein-pprint "1.2.0"]]

  :deploy-repositories  [["clojars" {:url "https://clojars.org/repo"
                                     :username :env/clojars_username
                                     :password :env/clojars_password
                                     :sign-releases false}]
                         ["snapshots" :clojars]
                         ["releases"  :clojars]]

  :source-paths    ["src/clj"]
  :target-path     "target/%s"

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [compojure "1.6.1"]
                                  [ring "1.7.1"]]}
             :test {:test-paths ["src_test/clj"]
                    :jvm-opts   ["-Dorg.eclipse.jetty.LEVEL=OFF"]}})
