(ns com.palletops.leinout.core
  (:require
   [clojure.string :as string :refer [trim]]))

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
