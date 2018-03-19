(ns def-async-test.core-test
  (:require [def-async-test.core :refer :all]
            [clojure.test :refer :all]
            [clojure.core.async :refer [chan >!!]]))

(def-async-test "when things run correctly" {}
  (is (= 1 1)))

(def-async-test "when there's a async test" {}
  (let [c (chan)]
    (future
     (Thread/sleep 200)
     (>!! c "ok"))
    (is (= "ok" (await! c)))))

(run-tests)
