(ns check.async
  (:require [clojure.string :as str]
            [check.core :as core]
            [clojure.test :as test :include-macros true]
            [clojure.core.async :as async :include-macros true]
            [net.cgrand.macrovich :as macros]))

(def ^:dynamic timeout 3000)
(defmacro async-test
  "Defines an async test. On Clojure, wraps all execution over a go-block.
On ClojureScript, wraps execution on a go-block and also sets the `async`
option. If the first argument of `cmds` is a map, it'll accept the following
keys:

:teardown - a sequence of commands that will tear down after the execution
of every async test, both on success or on failure
:timeout - the time that the async test will wait before timing out"
  [description & cmds]
  (let [opts (first cmds)
        cmds (cond-> cmds (map? opts) rest)]
    (macros/case
      :cljs
      `(test/async done#
         (test/testing ~description
           (async/go
            (let [mark-as-done# (delay
                                  ~(if-let [teardown (:teardown opts)]
                                     teardown)
                                  (done#))]
              (js/setTimeout (fn []
                               (when-not (realized? mark-as-done#)
                                 (test/is (throw (ex-info "Async test error - not finalized" {})))
                                 @mark-as-done#))
                             ~(:timeout opts 3000))
              ~@cmds
              @mark-as-done#))))
      :clj
      `(test/testing ~description
         (binding [timeout ~(:timeout opts 3000)]
           (try
             ~@cmds
             (finally
               ~(if-let [teardown (:teardown opts)]
                  teardown))))))))

(defmacro await! [chan]
  (macros/case
   :cljs `(async/<! ~chan)
   :clj `(first (async/alts!! [~chan (async/timeout timeout)]))))

(defmacro await-all! [chans]
  (macros/case
    :cljs `(async/alts! ~chans)
    :clj `(async/alts!! ~chans)))

(defn- to-chan [left]
  `(let [chan# (async/promise-chan)]
       (.then ~left (fn [result#] (async/put! chan# result#)))
       chan#))

(defmethod core/assert-arrow '=resolves=> [cljs? left _ right]
  (if cljs?
    `(let [chan# (if (instance? js/Promise ~left)
                   ~(to-chan left)
                   ~left)]
       (core/assert-arrow true (await! chan#) ~''=> ~right))))
