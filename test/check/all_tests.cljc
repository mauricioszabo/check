(ns check.all-tests
  (:require [check.core-test]
            [check.async-test]
            [clojure.test :refer [run-all-tests]]))

(defn main []
  (run-all-tests))

(defn ^:dev/after-load after []
  (main))
