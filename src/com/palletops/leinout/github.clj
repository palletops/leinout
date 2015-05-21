(ns com.palletops.leinout.github
  "Github interaction"
  (:require
   [clojure.java.io :refer [file]]
   [clojure.string :refer [blank? join]]
   [com.palletops.leinout.core :refer [with-system-out-str]]
   [leiningen.core.eval :as eval]
   [tentacles.orgs :as orgs]
   [tentacles.repos :as repos]))

(defn- login-string
  "Return a string that can be passed to tentacles via auth."
  [login-pw-seq]
  (join ":" (remove blank? login-pw-seq)))

(defn- keychain-for
  "Use the keychain to obtain credentials."
  [server]
  (if (.exists (file "/usr/bin/security"))
    (let [r (atom nil)
          result (with-system-out-str
                   ;; password is output on stderr
                   (binding [*err* *out*]
                     (reset!
                      r
                      (eval/sh
                       "/usr/bin/security" "find-internet-password"
                       "-s" server "-g"))))]
      (if (zero? @r)
        [(second (re-find #"\"acct\"<blob>=\"(.*)\"" result))
         (second (re-find #"password: \"(.*)\"" result))]))))

(defn- authinfo-for
  "Return a sequence with login and possibly a password from .authinfo."
  [server]
  (let [text (with-system-out-str
               (eval/sh
                (or (System/getenv "LEIN_GPG") "gpg")
                "-q" "--no-tty" "-d"
                (str (System/getProperty "user.home") "/.authinfo.gpg")))
        m (re-find
           (re-pattern
            (format
             "machine %s login ([^ \\n]+)(?: password ([^ \\n]+))?"
             server))
           text)]
    (drop 1 m)))

(defn github-login
  "Return a github login key from .authinfo.gpg or from the keychain."
  []
  (login-string (or (authinfo-for "github.com")
                    (keychain-for "github.com"))))

(defn team-id
  "Return the id of a team for org, given `token`."
  [team-slug org token]
  (->> (orgs/teams org {:auth token})
       (filter #(= team-slug (:slug %)))
       first
       :id))

(defn team-repos
  [id token]
  (orgs/list-team-repos id {:auth token}))

(defn repo-matching
  "Return the repo matching the git or https url for the repo."
  [repos url]
  (first (filterv #((set ((juxt :ssh_url :clone_url) %)) url) repos)))

(defn url->repo
  "Return a partial repository map from a url"
  [^String url]
  (let [x (or (re-matches #"git@github.com:([^/]+)/(.+).git" url)
              (re-matches #"git://github.com/([^/]+)/(.+).git" url)
              (re-matches #"https://github.com/([^/]+)/(.+).git" url))]
    (cond-> {:login (second x)
             :name (last x)}
            (.startsWith url "git@") (assoc :ssh_url url)
            (.startsWith url "git:") (assoc :git_url url)
            (.startsWith url "https") (assoc :clone_url url))))

(defn auth-team-id
  "Ensure the team id is authorised on the repository identified by
  the git url string."
  [team-id {:keys [login name] :as repo} token]
  (let [repos (team-repos team-id token)
        url ((some-fn :ssh_url :git_url :clone_url) repo)]
    (if-not (repo-matching repos url)
      (if (orgs/add-team-repo team-id login name {:auth token})
        :authorised
        :authorisation-failed)
      :already-authorised)))

(defn get-file-str
  "Get the contents of a file from the develop branch as a string."
  [{:keys [login name] :as repo} path]
  (let [r (repos/contents login name path {:str? true :ref "develop"})]
    (if (:status r)
      (throw
       (ex-info
        (format "Could not retrieve %s. %s" path (-> r :body :message))
        r))
      (:content r))))
