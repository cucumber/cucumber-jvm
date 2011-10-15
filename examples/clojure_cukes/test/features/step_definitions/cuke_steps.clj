(use 'clojure-cukes.core)
(use 'clojure.test)

(Given #"^I have (\d+) big \"([^\"]*)\" in my belly$"
       (fn [n, thing]
         (reset! belly (repeat (read-string n) thing))))

(When #"I eat (\d+) \"([^\"]*)\""
      (fn [n, thing]
        (eat (repeat (read-string n) thing))))

(Then #"^I am \"([^\"]*)\"$"
      (fn [mood-name]
        (is (= (name (mood)) mood-name))))
