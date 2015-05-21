(ns com.palletops.leinout.git-test
  (:refer-clojure :exclude [merge])
  (:require [com.palletops.leinout.git :refer :all]
            [clojure.test :refer :all]))

(deftest remotes-test
  (let [remotes (remotes)]
    (is (seq remotes))
    (is (every? string? (keys remotes)))))

(deftest current-sha-test
  (let [sha (current-sha)]
    (is (string? sha))
    (is (pos? (count sha)))))

(deftest current-branch-test
  (let [branch (current-branch)]
    (is (string? branch))
    (is (pos? (count branch)))))
