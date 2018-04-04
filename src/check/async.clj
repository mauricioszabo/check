(ns check.async
  (:require [clojure.string :as str]
            [check.utils :refer [if-cljs]]
            [expectations :refer [compare-expr ->failure-message in]]
            [clojure.test :refer [deftest testing do-report is assert-expr]]
            [clojure.core.async :refer [close! chan >! <! go alts!! timeout]]))

(defmacro await! [chan]
  `(first (alts!! [~chan (timeout 3000)])))

(defmacro await-all! [chans]
  `(alts!! ~chans))

(defmacro def-async-test [description opts & cmds]
  (assert (map? opts) "second parameter must be a map")
  (let [norm-desc (symbol (-> description
                              (str/replace #"\s" "-")
                              (str/replace #"[^\-\w\d]" "")))]
    `(deftest ~norm-desc
       (testing ~description
         (try
           ~@cmds
           (finally
             ~(if-let [teardown (:teardown opts)]
                teardown)))))))
