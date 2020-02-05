(ns check.async
  (:require-macros [check.async])
  (:require [clojure.string :as str]
            [check.core :as core]
            [clojure.test :as test :include-macros true]
            [clojure.core.async :as async :include-macros true]
            [promesa.core :as p]
            [net.cgrand.macrovich :as macros]))

(def ^:dynamic timeout 3000)
(defn- to-chan [left]
  `(let [chan# (async/promise-chan)]
       (.then ~left (fn [result#] (async/put! chan# result#)))
       chan#))

(defn get-from-channel! [cljs? chan]
  (if cljs?
   `(async/<! (if (instance? js/Promise ~chan)
                ~(to-chan chan)
                ~chan))
   `(first (async/alts!! [~chan (async/timeout timeout)]))))

(defmethod core/assert-arrow '=resolves=> [cljs? left _ right]
  `(let [value# (get-from-channel! ~cljs? ~left)]
     (core/assert-arrow ~cljs? value# '=> ~right)))

(defn promise-test* [prom done tear time]
  (let [teardown (or tear #())
        mark-done (delay (do (teardown) (done)))]
    (js/setTimeout (fn [] (when-not (realized? mark-done)
                            (throw (ex-info "Async error - not finalized" {}))
                            @mark-done))
                   time)
    (.then prom (fn [_] @mark-done))
    (.catch prom (fn [error]
                   @done
                   (throw (ex-info "Promise resolved with error" {:error error}))))))
