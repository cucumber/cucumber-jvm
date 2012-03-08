(use 'cucumber.runtime.clojure.belly)

(Before)
(After)

(Given #"^I have (\d+) cukes in my belly$" [cukes-count]
  (eat (Float. cukes-count)))

(When #"^there are (\d+) cukes in my belly$" [expected]
  (assert (= (last-meal) (Float. expected))))

(Then #"^the (.*) contains (.*)$" [container ingredient]
  (assert (= "glass" container)))

(When #"^I add (.*)$" [liquid]
  (assert (= "milk" liquid)))
