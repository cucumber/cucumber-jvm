(use 'clojure-cukes.core)
(use 'clojure.test)

(Given #"^I have (\d+) big \"([^\"]*)\" in my belly$" [n, thing]
       (reset! belly (repeat (read-string n) thing)))

(When #"I eat (\d+) \"([^\"]*)\"" [n, thing]
      (eat (repeat (read-string n) thing)))

(Then #"^I am \"([^\"]*)\"$" [mood-name]
      (assert (= (name (mood)) mood-name)))
