; TODO: Not sure how to deal with this in a functional
; programming language like Clojure - we can't *store*
; the entered values anywhere...
; Need some expert help here...

(Given #"I have entered (\d+) into the calculator"
  (fn [n]
    (println "Entered: " n)))

(When #"I press (\w+)"
  (fn [btn]
    (println "Pressed: " btn)))

(Then #"the stored result should be (.*)"
  (fn [res]
    (println "Expected: " res)))
