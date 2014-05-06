(ns com.palletops.leinout.git
  (:refer-clojure :exclude [merge])
  (:require
   [clojure.string :as string :refer [blank? split-lines trim]]
   [leiningen.core.eval :as eval]
   [leiningen.core.main :refer [debug]]
   [com.palletops.leinout.core :refer [fail fail-on-error]]))

(defn ensure-origin
  "Ensure there is a git remote named 'origin'."
  []
  (when (pos? (eval/sh "git" "remote" "show" "origin"))
    (fail "No origin repository.  Have you created it on github?")))

(defn add-remote
  "Add a remote to the repository."
  [remote url]
  (debug "git remote add" remote url)
  (fail-on-error (eval/sh "git" "remote" "add" remote url)))

(defn remotes
  "List remotes on the repository.  Return a map keyed on remote name,
  with values being a map with `:fetch` and `:push` keys and url
  string values."
  []
  (debug "git remote -v")
  (reduce
   (fn parse-remotes [m l]
     (let [[_ name url direction] (re-matches
                                   #"([^ ]+)\s+([^ ]+)\s+\(([^ ]+)\)"
                                   l)]
       (assoc-in m [name (keyword direction)] url)))
   {}
   (-> (with-out-str (fail-on-error (eval/sh "git" "remote" "-v")))
       split-lines)))

(defn origin
  "Return the origin repository url string for the specified
  direction (:fetch or :push)"
  ([direction]
     {:pre [(#{:fetch :push} direction)]}
     (get-in (remotes) ["origin" direction]))
  ([] (origin :fetch)))

(defn tag
  "Tag a repository."
  [& args]
  (apply debug "git tag" args)
  (fail-on-error (apply eval/sh "git" "tag" args)))

(defn add
  "Add files to be staged."
  [& args]
  (apply debug "git add" args)
  (fail-on-error (apply eval/sh "git" "add" args)))

(defn commit
  "Commit staged files with the specified commit message."
  [msg]
  (debug "git commit -m" "\"" msg "\"")
  (fail-on-error (eval/sh "git" "commit" "-m" msg)))

(defn push
  "Push the `branch-spec` to `remote`."
  [remote branch-spec]
  (debug "git push" remote branch-spec)
  (fail-on-error (eval/sh "git" "push" remote branch-spec)))

(defn checkout
  "Checkout a branch."
  [& args]
  (apply debug "git checkout" args)
  (fail-on-error (apply eval/sh "git" "checkout" args)))

(defn pull
  "Pull."
  []
  (debug "git pull")
  (fail-on-error (eval/sh "git" "pull")))

(defn fetch
  "Fetch."
  [& args]
  (apply debug "git fetch" args)
  (fail-on-error (apply eval/sh "git" "fetch" args)))

(defn merge
  "Merge"
  [& args]
  (apply debug "git merge" args)
  (fail-on-error (apply eval/sh "git" "merge" args)))

(defn current-sha
  "Return the SHA for the current HEAD."
  []
  (debug "git rev-parse HEAD")
  (trim (with-out-str (fail-on-error
                       (eval/sh "git" "rev-parse" "HEAD")))))

(defn ^String current-branch
  "Return the name of the current branch."
  []
  (debug "git current-branch")
  (let [sha (current-sha)
        out (with-out-str (fail-on-error
                           (eval/sh "git" "branch" "-v" "--no-abbrev")))
        line (->> out
                  (string/split-lines)
                  (filter #(.contains ^String % sha))
                  (remove #(.contains ^String % "detached"))
                  first)]
    (->> (string/split (string/replace line "*" "") #" +")
         (remove blank?)
         first)))

(defn config
  "Set the global git configuration, given a map of key and values."
  [kvs]
  (debug "git config --global" (pr-str kvs))
  (doseq [[k v] kvs]
    (fail-on-error (eval/sh "git" "config" "--global" (name k) v))))
