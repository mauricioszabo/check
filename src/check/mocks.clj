(ns check.mocks
  (:require [clojure.spec.alpha :as s]))

(defn- normalize [[fun args+return]]
  [fun (fn [ & old-args]
         (if-let [return (get args+return old-args)]
           return
           (throw (ex-info "No mocked calls for this fn/args"
                           {:function fun
                            :expected-args args
                            :actual-args old-args}))))])

(defn mocking* [fun+args+returns body]
  (let [mocks (->> fun+args+returns
                   (map normalize)
                   (into {}))]
    (with-redefs-fn mocks
      body)))

(s/def ::arrow '#{=>})
(s/def ::template (s/cat :fn symbol? :args (s/* any?)))
(s/def ::mocks (s/cat
                :mocks (s/+ (s/cat :template (s/spec ::template) :arrow ::arrow :return any?))
                :arrow '#{--- ===}
                :body (s/* any?)))

(defn- normalize-mocking-params [mockings]
  (->> mockings
       (map (fn [{:keys [template return arrow]}]
              [`(var ~(:fn template)) {:args (:args template) :return return}]))
       (group-by first)
       (map (fn [[k v]]
              [k (->> v
                      (map (fn [[_ v]] [(:args v) (:return v)]))
                      (into {}))]))
       (into {})))

(defmacro mocking
  "Mocks a group of calls. "
  [ & args]
  (s/assert* ::mocks args)
  (let [{:keys [mocks body]} (s/conform ::mocks args)
        mockings (normalize-mocking-params mocks)]
    `(mocking* ~mockings (fn [] ~@body))))
