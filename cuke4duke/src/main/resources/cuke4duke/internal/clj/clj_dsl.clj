(defn stepdef [regexp, closure]
  (. cuke4duke.internal.clj.CljLanguage (addStepDefinition regexp, closure)))

(def Given stepdef)
(def When stepdef)
(def Then stepdef)