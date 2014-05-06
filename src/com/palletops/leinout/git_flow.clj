(ns com.palletops.leinout.git-flow
  "Interact with git-flow."
  (:require
   [clojure.string :as string :refer [blank? split-lines trim]]
   [leiningen.core.eval :as eval]
   [leiningen.core.main :refer [debug]]
   [com.palletops.leinout.core :refer [fail fail-on-error]]))

(defn ensure-git-flow
  "Ensure git-flow is enabled on the repository."
  []
  (let [m (with-out-str
            (eval/sh "git" "config" "--get" "gitflow.branch.master"))]
    (when (blank? m)
      (fail-on-error (eval/sh "git" "flow" "init" "-d")))))

(defn release-start
  "Start a release branch using git-flow."
  [version]
  (debug "git flow release start" version)
  (with-out-str                         ; suppress output
    (fail-on-error (eval/sh "git" "flow" "release" "start" version))))
