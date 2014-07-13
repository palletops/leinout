(defproject com.palletops/leinout "0.1.2-SNAPSHOT"
  :description "A library for doing things with git, github, travis and lein."
  :url "https://github.com/palletops/leinout"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[tentacles "0.2.5" :exclusions [clj-http]]]
  :profiles {:provided {:dependencies [[org.clojure/clojure "1.6.0"]
                                       [leiningen "2.3.4"]]}})
