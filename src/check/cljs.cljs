(ns check.cljs
  (:require-macros [check.cljs :as c])
  (:require [check.arrows :refer [assert-arrow]]
            [clojure.test :refer [do-report]]))

(defmethod check.arrows/assert-arrow '=throws=> [left _ right]
  `(try
     (let [res# ~left]
       {:type :error
        :message (str "Expected " (quote ~left) " to throw error " (quote ~right))
        :expected ~right
        :actual res#})
     (catch js/Object t#
       (c/check t# ~'=> ~right))))
