(ns check.async
  (:require [clojure.string :as str]))

#?(:cljs
   (require-macros '[check.utils :refer [if-cljs]]
                   '[cljs.core.async.macros :refer []])
   :clj
   (require '[check.utils :refer [if-cljs]]))

#?(:cljs
   (require '[cljs.core.async :refer [chan >! <! alts!]]
            '[clojure.test :refer-macros [deftest async testing is]]
            '[expectations :refer [compare-expr ->failure-message in]]
            '[clojure.test :refer [do-report]]))

#?(:clj
   (require '[expectations :refer [compare-expr ->failure-message in]]
            '[clojure.test :refer [deftest testing do-report is assert-expr]]
            '[clojure.core.async :refer [close! chan >! <! go]]))

(defmacro await! [chan]
  `(if-cljs
     (cljs.core.async/<! ~chan)
     (first (clojure.core.async/alts!! [~chan (clojure.core.async/timeout 3000)]))))

(defmacro await-all! [chans]
  `(if-cljs
     (cljs.core.async/alts! ~chans)
     (clojure.core.async/alts!! ~chans)))

(defmacro def-async-test [description opts & cmds]
  (assert (map? opts) "second parameter must be a map")
  (let [norm-desc (symbol (-> description
                              (str/replace #"\s" "-")
                              (str/replace #"[^\-\w\d]" "")))]
    `(if-cljs
       (deftest ~norm-desc
         (cljs.test/async done#
                          (cljs.core.async.macros/go
                           (let [mark-as-done# (delay
                                                ~(if-let [teardown (:teardown opts)]
                                                   teardown)
                                                (done#))]
                             (testing ~description
                               (js/setTimeout (fn []
                                                (when-not (realized? mark-as-done#)
                                                  (cljs.test/is (throw "Async test error - not finalized"))
                                                  @mark-as-done#))
                                              3000)
                               ~@cmds
                               @mark-as-done#)))))
       (deftest ~norm-desc
         (testing ~description
           (try
             ~@cmds
             (finally
               ~(if-let [teardown (:teardown opts)]
                  teardown))))))))
