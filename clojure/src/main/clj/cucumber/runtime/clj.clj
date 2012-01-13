(ns cucumber.runtime.clj
  (:import (cucumber.runtime CucumberException
                             JdkPatternArgumentMatcher
                             StepDefinition)
           (clojure.lang RT))
  (:gen-class :name cucumber.runtime.clj.Backend
              :implements [cucumber.runtime.Backend]
              :constructors
              {[cucumber.io.ResourceLoader] []}
              :init init
              :state state))

;; maybe this should be an atom
(def world (promise))

(defn load-script [path]
  (try
    (RT/load (str "cucumber/" (.replaceAll path ".clj$" "")) true)
    (catch Throwable t
      (throw (CucumberException. t)))))

(defn- -init [resource-loader]
  [[] (atom {:resource-loader resource-loader})])

(defn- -buildWorld [cljb glue-paths a-world]
  (deliver world a-world)
  (doseq [path glue-paths
          resource (.resources (:resource-loader @(.state cljb)) path ".clj")]
    ;; scripts are loaded with this namespace as the current namespace
    ;; to give access to the macros defined below
    ;; you can still use (ns ...) as normal
    (binding [*ns* (create-ns 'cucumber.runtime.clj)]
      (load-script (.getPath resource)))))

(defn- -disposeWorld [cljb])

;; Snippets don't have a nice interface to extend :(
(defn- -getSnippet [cljb step]
  "snippet!")

(defn add-step-definition [pattern fun location]
  (.addStepDefinition
   @world
   (reify
     StepDefinition
     (matchedArguments [_ step]
       (.argumentsFrom (JdkPatternArgumentMatcher. pattern)
                       (.getName step)))
     (getLocation [_]
       (str (:file location) ":" (:line location)))
     (getParameterTypes [_]
       nil)
     (execute [_ locale args]
       (apply fun args))
     (isDefinedAt [_ stack-trace-element]
       (and (= (.getLineNumber stack-trace-element)
               (:line location))
            (= (.getFileName stack-trace-element)
               (:file location))))
     (getPattern [_]
       pattern))))

;; TODO: before and after hooks

(defmacro step-macros [& names]
  (cons 'do
        (for [name names]
          `(defmacro ~name [pattern# fun#]
             `(add-step-definition ~pattern# ~fun#
                                   '~{:file *file*
                                      :line (:line (meta ~'&form))})))))
(step-macros
 Given When Then And But)
