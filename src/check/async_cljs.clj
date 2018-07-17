(ns check.async-cljs
  (:require [clojure.string :as str]
            [cljs.core.async :as async]
            [clojure.test :as test]
            [expectations :refer [compare-expr ->failure-message in]]
            [clojure.test :refer [do-report]]))

(defmacro await! [chan]
  `(async/<! ~chan))

(defmacro await-all! [chans]
  `(async/alts! ~chans))

(defmacro def-async-test [description opts & cmds]
  (assert (map? opts) "second parameter must be a map")
  (let [norm-desc (symbol (-> description
                              (str/replace #"\s" "-")
                              (str/replace #"[^\-\w\d]" "")))]
    `(test/deftest ~norm-desc
       (test/async done#
         (async/go
          (let [mark-as-done# (delay
                               ~(if-let [teardown (:teardown opts)]
                                  teardown)
                               (done#))]
            (test/testing ~description
              (js/setTimeout (fn []
                               (when-not (realized? mark-as-done#)
                                 (test/is (throw (ex-info "Async test error - not finalized" {})))
                                 @mark-as-done#))
                             3000)
              ~@cmds
              @mark-as-done#)))))))
