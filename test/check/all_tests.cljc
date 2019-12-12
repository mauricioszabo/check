(ns check.all-tests
  (:require [check.core-test]
            [clojure.test :refer [run-all-tests]]))


(defn main []
  (run-all-tests))

(defn ^:dev/after after-reload []
  (prn :RELOADED))
