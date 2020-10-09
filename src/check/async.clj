(ns check.async
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [check.core :as core]
            [clojure.test :as test]
            [clojure.core.async :as async]
            [promesa.core :as p]
            [clojure.walk :as walk]
            [net.cgrand.macrovich :as macros]
            [clojure.core.async.impl.protocols :as proto])
  (:import [clojure.core.async.impl.protocols ReadPort]))

(def ^:dynamic timeout 3000)
(defn- get-from-channel! [chan]
  `(cond
     (promesa.core/promise? ~chan) (async/go @~chan)
     (instance? ReadPort ~chan) ~chan
     :else (async/go ~chan)))

(defmacro await! [chan]
  (macros/case
   :cljs `(to-promise ~chan)
   :clj `(async/<!! ~(get-from-channel! chan))))

(defmacro testing [description & body]
  (macros/case
   :clj `(test/testing ~description ~@body)
   :cljs `(test/testing ~description (promesa.core/do! ~@body))))

(defn async-test* [description timeout teardown-delay go-thread]
  (test/testing description
    (let [[res] (async/alts!! [go-thread
                               (async/thread (async/<!! (async/timeout timeout)) ::timeout)]
                             :priority true)]
      (case res
        ::ok @teardown-delay
        ::timeout (do
                    @teardown-delay
                    (throw (ex-info "Async error - not finalized" {:timeout timeout})))
        (do
          @teardown-delay
          (throw res))))))

(s/def ::description string?)
(s/def ::teardown fn?)
(s/def ::timeout int?)
(s/def ::params map? #_(s/keys :opt-un [::timeout ::teardown]))
(s/def ::body any?)
(s/def ::full-params
  (s/cat :description ::description
         :params (s/? ::params)
         :body (s/* ::body)))

(defmacro async-test [ & params]
  (let [{:keys [description params body]} (s/conform ::full-params params)
        timeout (:timeout params 2000)
        teardown (:teardown params nil)]
    (macros/case
     :clj `(async-test* ~description ~timeout
             (delay ~teardown)
             (async/thread (try ~@body ::ok (catch Throwable t# t#))))
     :cljs `(async-test* ~description ~timeout (fn [] ~teardown)
              (promesa.core/do! ~@body)))))
(s/fdef async-test :args (s/cat :params ::full-params))

(defmacro check [left arrow right]
  (let [cljs? (macros/case :cljs true :clj false)
        lft (gensym "left-")
        rgt (gensym "right-")]
    (macros/case
     :clj `(let [~lft (await! ~left)
                 ~rgt (await! ~right)]
             (test/do-report (assoc ~(core/assert-arrow cljs? lft arrow rgt)
                                    :expect (quote ~right))))
     :cljs `(promesa.core/let [~lft (await! ~left) ~rgt (await! ~right)]
              (test/do-report (assoc ~(core/assert-arrow cljs? lft arrow rgt)
                                     :expected (quote ~right)))
              :done))))
