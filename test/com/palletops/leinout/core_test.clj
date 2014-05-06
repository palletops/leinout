(ns com.palletops.leinout.core-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.leinout.core :refer :all]))

(deftest deep-merge-test
  (is (= {:a 1 :b {:v :w :x :y} :c [1 2 3 4]}
         (deep-merge
          {:a 1 :b {:x :y} :c [1 2]}
          {:a 1 :b {:v :w} :c [3 4]}))))
