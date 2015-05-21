(ns com.palletops.leinout.core
  (:require
   [clojure.string :as string :refer [trim]])
  (:import
   [java.io ByteArrayOutputStream PrintStream]))

(defn deep-merge
  "Recursively merge maps.  Sub-maps are recursively merged. Vectors
  are merged by concat."
  [& ms]
  (letfn [(f [a b]
            (if (and (map? a) (map? b))
              (deep-merge a b)
              (if (and (vector? a) (vector? b))
                (vec (concat a b))
                (or b a))))]
    (apply merge-with f ms)))

(defn fail
  "Fail with the given message, msg."
  [msg]
  (throw (ex-info msg {:exit-code 1})))

(defn fail-on-error
  "Fail on a shell error."
  [exit]
  (when (and exit (pos? exit))
    (fail "Shell command failed")))

;; Modified version of same named function from leiningen's test helper.clj.
;; The added complication here is to use with-out-str within the expansion,
;; so that it works on any version of leiningen.
(defmacro with-system-out-str
  "Like with-out-str, but for System/out."
  [& body]
  `(let [orig# System/out]
     (try (let [o# (ByteArrayOutputStream.)]
            (System/setOut (PrintStream. o#))
            (str (with-out-str ~@body) (.toString o#)))
          (finally
            (System/setOut orig#)))))
