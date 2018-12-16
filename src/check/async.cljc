(ns check.async
  (:require [clojure.string :as str]
            [clojure.test :as test :include-macros true]
            [clojure.core.async :as async :include-macros true]
            [net.cgrand.macrovich :as macros]))

(defmacro def-async-test [description opts & cmds]
  (assert (map? opts) "second parameter must be a map")
  (let [norm-desc (symbol (-> description
                              (str/replace #"\s" "-")
                              (str/replace #"[^\-\w\d]" "")))]
    (macros/case
      :cljs
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
                @mark-as-done#)))))
      :clj
      `(test/deftest ~norm-desc
         (test/testing ~description
           (try
             ~@cmds
             (finally
               ~(if-let [teardown (:teardown opts)]
                  teardown))))))))

(defmacro await! [chan]
  (macros/case
   :cljs `(async/<! ~chan)
   :clj `(first (async/alts!! [~chan (async/timeout 3000)]))))

(defmacro await-all! [chans]
  (macros/case
    :cljs `(async/alts! ~chans)
    :clj `(async/alts!! ~chans)))
;
