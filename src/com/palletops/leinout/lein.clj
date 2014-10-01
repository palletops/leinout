(ns com.palletops.leinout.lein
  "Leiningen configuration for palletops release process"
  (:refer-clojure :exclude [test])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :refer [file]]
   [leiningen.core.main
    :refer [resolve-and-apply debug info *exit-process?*]])
  (:import
   java.io.File))

(defn ^File profiles-clj-file
  [{:keys [root] :as project}]
  {:pre [root]}
  (file root "profiles.clj"))

(defn read-profiles
  "Returns the profiles in the project profiles.clj"
  [^File f]
  {:pre [f]}
  (if (.exists f)
    (edn/read-string (slurp f))))

(defn release-repo-coordinates [project]
  (-> project :pallet-release :url))

(defn uses-lein-modules?
  [project]
  (some #(= 'lein-modules/lein-modules (first %)) (:plugins project)))

(defn task
  "Apply a task to project, using lein-modules if it is enabled."
  [project & args]
  (let [args (if (uses-lein-modules? project)
               (concat ["modules"] args)
               args)]
    (resolve-and-apply project args)))

(defn clean
  [project]
  (debug "lein clean")
  (task project "clean"))

(defn test
  [project]
  (debug "lein test")
  (task project "with-profile" "+no-checkouts" "test"))

(defn update-versions
  [project old-version new-version]
  (debug "lein set-version" new-version)
  (task project "with-profile"
        "+release" "set-version"
        new-version ":previous-version" old-version))

(defn set-next-version
  [project]
  (debug "lein set-version :point")
  (task project "with-profile" "+release" "set-version" ":point"))

(defn deploy
  [project & args]
  (debug "lein deploy")
  (apply task project "deploy" args))

(defn pom
  [project]
  (debug "lein pom")
  (task project "pom"))

(defn check
  [project]
  (debug "lein check")
  (binding [*exit-process?* false]
    (task project "check")))
