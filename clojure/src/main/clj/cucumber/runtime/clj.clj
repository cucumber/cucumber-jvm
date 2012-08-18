(ns cucumber.runtime.clj
  (:require (clojure [string :as str]))
  (:import (cucumber.runtime CucumberException
                             JdkPatternArgumentMatcher
                             StepDefinition
                             HookDefinition)
           (cucumber.runtime.snippets Snippet
                                      SnippetGenerator)
           (gherkin TagExpression)
           (clojure.lang RT))
  (:gen-class :name cucumber.runtime.clj.Backend
              :implements [cucumber.runtime.Backend]
              :constructors
              {[cucumber.io.ResourceLoader] []}
              :init init
              :state state))

(def glue (atom nil))

;; Make sure ClojureSnippet.java is in sync with this - for testing purposes.
(defn clojure-snippet []
  (reify
    Snippet
    (template [_]
      (str
       "({0} #\"{1}\" [{3}]\n"
       "  (comment  {4}  )\n"
       "  (throw (cucumber.runtime.PendingException.)))\n"))
    (arguments [_ argumentTypes]
      (str/replace (SnippetGenerator/untypedArguments argumentTypes)
                   "," ""))
    (namedGroupStart [_] nil)
    (namedGroupEnd [_] nil)
    (tableHint [_] nil)
    (escapePattern [_ pattern]
      (str/replace (str pattern) "\"" "\\\""))))

(def snippet-generator (SnippetGenerator. (clojure-snippet)))

(defn load-script [path]
  (try
    (RT/load (str (.replaceAll path ".clj$" "")) true)
    (catch Throwable t
      (throw (CucumberException. t)))))

(defn- -init [resource-loader]
  [[] (atom {:resource-loader resource-loader})])

(defn -loadGlue [cljb a-glue glue-paths]
  (reset! glue a-glue)
  (doseq [path glue-paths
          resource (.resources (:resource-loader @(.state cljb)) path ".clj")]
    (binding [*ns* (create-ns 'cucumber.runtime.clj)]
      (load-script (.getPath resource)))))

(defn- -buildWorld [cljb])

(defn- -disposeWorld [cljb])

(defn- -getSnippet [cljb step]
  (.getSnippet snippet-generator step))

(defn- -setUnreportedStepExecutor [cljb executor]
  "executor")

(defn add-step-definition [pattern fun location]
  (.addStepDefinition
   @glue
   (reify
     StepDefinition
     (matchedArguments [_ step]
       (.argumentsFrom (JdkPatternArgumentMatcher. pattern)
                       (.getName step)))
     (getLocation [_ detail]
       (str (:file location) ":" (:line location)))
     (getParameterCount [_]
       nil)
     (getParameterType [_ n argumentType]
       nil)
     (execute [_ locale args]
       (apply fun args))
     (isDefinedAt [_ stack-trace-element]
       (and (= (.getLineNumber stack-trace-element)
               (:line location))
            (= (.getFileName stack-trace-element)
               (:file location))))
     (getPattern [_]
       (str pattern)))))

(defmulti add-hook-definition (fn [t & _] t))

(defmethod add-hook-definition :before [_ tag-expression hook-fun]
  (let [te (TagExpression. tag-expression)]
    (.addBeforeHook
     @glue
     (reify
       HookDefinition
       (execute [hd scenario-result]
         (hook-fun))
       (matches [hd tags]
         (.eval te tags))
       (getOrder [hd] 0)))))

(defmethod add-hook-definition :after [_ tag-expression hook-fun]
  (let [te (TagExpression. tag-expression)
        max-parameter-count (->> hook-fun class .getDeclaredMethods
                                 (filter #(= "invoke" (.getName %)))
                                 (map #(count (.getParameterTypes %)))
                                 (apply max))]
    (.addAfterHook
     @glue
     (reify
       HookDefinition
       (execute [hd scenario-result]
         (if (zero? max-parameter-count)
           (hook-fun)
           (hook-fun scenario-result)))
       (matches [hd tags]
         (.eval te tags))
       (getOrder [hd] 0)))))

(defmacro step-macros [& names]
  (cons 'do
        (for [name names]
          `(defmacro ~name [pattern# binding-form# & body#]
             `(add-step-definition ~pattern#
                                   (fn ~binding-form# ~@body#)
                                   '~{:file *file*
                                      :line (:line (meta ~'&form))})))))
(step-macros
 Given When Then And But)


(defmacro Before [binding-form & body]
  `(add-hook-definition :before [] (fn ~binding-form ~@body)))

(defmacro After [binding-form & body]
  `(add-hook-definition :after [] (fn ~binding-form ~@body)))

(defn ^:private update-keys [f m]
  (reduce-kv #(assoc %1 (f %2) %3) {} m))

(defn ^:private update-values [f m]
  (reduce-kv #(assoc %1 %2 (f %3)) {} m))

(defn read-cuke-str
  "Using the clojure reader is often a good way to interpret literal values
   in feature files. This function makes some cucumber-specific adjustments
   to basic reader behavior. This is particulary appropriate when reading a
   table, for example: reading | \"1\" | 1 | we should intepret 1 as an int
   and \"1\" as a string. This is used by kv-table->map and table->rows."
  [string]
  (if (re-matches #"^:.*|\d+(\.\d+)?" string)
    (read-string string)
    (str/replace string #"\"" "")))

(defn kv-table->map
  "Reads a table of the form  | key | value |
   For example, given:
     | from      | 1293884100000 |
     | to        | 1293884100000 |
   It evaluates to the clojure literal:
     {:from 1293884100000, :to 1293884100000}"
  [data]
  (->> (into {} (map vec (.raw data)))
       (update-values read-cuke-str)
       (update-keys keyword)))

(defn table->rows
  "Reads a cucumber table of the form
     | key-1 | key-2 | ... | key-n |
     | val-1 | val-2 | ... | val-n |
   For example, given:
     | id | name    | created-at    |
     | 55 | \"foo\" | 1293884100000 |
     | 56 | \"bar\" | 1293884100000 |
   It evaluates to the clojure literal:
     [{:id 55, :name \"foo\", :created-at 1293884100000}
      {:id 56, :name \"bar\", :created-at 1293884100000}]"
  [data]
  (let [data (map seq (.raw data))
        header-keys (map keyword (first data))
        remove-blank (fn [m,k,v] (if (seq (str v)) (assoc m k v) m))
        row->hash (fn [row] (apply hash-map
                                   (interleave header-keys
                                               (map read-cuke-str row))))]
    (map (fn [row-vals] (reduce-kv remove-blank {} (row->hash row-vals)))
         (next data))))
