(ns com.palletops.leinout.github-test
  (:require [com.palletops.leinout.github :refer :all]
            [clojure.test :refer :all]))

(deftest origin-test
  (is (string? (github-login))))
