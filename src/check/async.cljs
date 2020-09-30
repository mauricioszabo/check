(ns check.async
  (:require-macros [check.async])
  (:require [clojure.string :as str]
            [check.core :as core]
            [clojure.test :as test]
            [clojure.core.async :as async]
            [promesa.core :as p]
            ; [net.cgrand.macrovich :as macros]
            [cljs.core.async.impl.protocols :as proto]))
  ; (:import [cljs.core.async.impl.protocols ReadPort]))

; (defn- to-chan [left]
;   `(let [chan# (async/promise-chan)]
;        (.then ~left (fn [result#] (async/put! chan# result#)))
;        chan#))
;
; (defn get-from-channel! [cljs? chan]
;   (if cljs?
;    `(async/<! (if (instance? js/Promise ~chan)
;                 ~(to-chan chan)
;                 ~chan))
;    `(first (async/alts!! [~chan (async/timeout timeout)]))))
;
; (defmethod core/assert-arrow '=resolves=> [cljs? left _ right]
;   `(let [value# (get-from-channel! ~cljs? ~left)]
;      (core/assert-arrow ~cljs? value# '=> ~right)))
;
; (defn promise-test* [prom done tear time]
;   (let [teardown (or tear #())
;         mark-done (delay (do (teardown) (done)))]
;     (js/setTimeout (fn [] (when-not (realized? mark-done)
;                             (throw (ex-info "Async error - not finalized" {}))
;                             @mark-done))
;                    time)
;     (.then prom (fn [_] @mark-done))
;     (.catch prom (fn [error]
;                    @done
;                    (throw (ex-info "Promise resolved with error" {:error error}))))))
;
(defn to-promise [promise-or-chan]
  (if (satisfies? proto/ReadPort promise-or-chan)
    (let [p (p/deferred)]
      (async/go
        (let [res (async/<! promise-or-chan)]
          (p/resolve! p res)))
      p)
    promise-or-chan))

(defn async-test* [description timeout teardown prom]
  (test/async done
    (let [timeout-prom (p/do!
                        (p/delay timeout)
                        (p/rejected (ex-info "Async test not finalized"
                                             {:timeout timeout})))]
      (test/testing description
        (-> (p/race [prom timeout-prom])
            (p/catch (fn [error] (test/is (not error)) nil))
            (p/then (fn [_] (teardown)))
            (p/catch (fn [error] (test/is (not error)) nil))
            (p/finally (fn [ & _] (done))))))))
