(use 'cucumber.runtime.clojure.belly)

(Before)
(After)

(Given #"^I have (\d+) cukes in my belly$"
  #(eat (Float. %1)))

(Given #"^I have this many cukes in my belly:$"
  #(doseq [x (.raw %1)] (eat (Float. (first x)))))

(When #"^there are (\d+) cukes in my belly$"
  (fn [expected]
    (assert (= (last-meal) (Float. expected)))))
    
(Then #"^the (.*) contains (.*)$"
  (fn [container, ingredient]
    (assert (= "glass" container))))

(When #"^I add (.*)$"
  (fn [liquid]
    (assert (= "milk" liquid))))
