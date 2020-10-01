(ns check.async
  (:require-macros [check.async])
  (:require [clojure.string :as str]
            [check.core :as core]
            [clojure.test :as test]
            [cljs.test]
            [clojure.core.async :as async]
            [promesa.core :as p]
            [cljs.core.async.impl.protocols :as proto]))

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
    (test/testing description
      (let [timeout-error (js/Object.)
            error (js/Object.)
            promise (p/catch prom #(vector error %))
            timeout-prom (p/do!
                          (p/delay timeout)
                          timeout-error)]
        (-> (p/race [promise timeout-prom])
            (p/then (fn [res]
                      (cond
                        (and (vector? res) (-> res first (= error)))
                        (test/do-report {:type :error
                                         :expected "Not an error"
                                         :actual (second res)})

                        (= timeout-error res)
                        (test/do-report {:type :error
                                         :expected "Test to finish"
                                         :actual (str "Test did not finish in " timeout "msec")}))

                      (teardown)))
            (p/catch (fn [error]
                       (test/is (not error))
                       nil))
            (p/finally (fn [ & _] (done))))))))
