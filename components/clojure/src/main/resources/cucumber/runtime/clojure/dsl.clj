(defn stepdef [regexp, closure]
  (. cucumber.runtime.clojure.ClojureBackend (addStepDefinition regexp, closure)))

(defmacro Before [& forms]
  `(. cucumber.runtime.clojure.ClojureBackend (addBeforeHook (fn [] ~@forms))))

(defmacro After [& forms]
  `(. cucumber.runtime.clojure.ClojureBackend (addAfterHook (fn [] ~@forms))))

(def Given stepdef)
(def When stepdef)
(def Then stepdef)
(def And stepdef)
(def But stepdef)
