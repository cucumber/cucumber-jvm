(defn stepdef [regexp closure]
  (. cucumber.runtime.clojure.ClojureBackend addStepDefinition regexp closure))

(defmacro stepdef-sugar [re binding-form & body]
  `(stepdef ~re (fn ~binding-form ~@body)))

(defmacro Before [& forms]
  `(. cucumber.runtime.clojure.ClojureBackend (addBeforeHook (fn [] ~@forms))))

(defmacro After [& forms]
  `(. cucumber.runtime.clojure.ClojureBackend (addAfterHook (fn [] ~@forms))))

(defmacro ^:private clone-macro [existing-macro new-macros]
  (cons 'do (mapcat
             (fn [m#]
               `((def ~m# #'~existing-macro)
                 (. #'~m# (setMacro))))
             new-macros)))

(clone-macro stepdef-sugar [Given When Then And But])
