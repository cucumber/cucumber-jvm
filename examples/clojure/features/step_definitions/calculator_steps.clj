(add-classpath (str "file://" (.. (java.io.File. *file*) getParentFile getParentFile getParent) "/lib/"))
(use 'calculator)


(Given #"^I have entered ([\d.]+) into the calculator$"
  (fn [number]
    (push-number (Float. number))))

(When #"^I press (\w+)$"
  (fn [btn]
    (calculate ({"divide" / "add" +} btn))))

(Then #"^the current value should be ([\d.]+)$"
  (fn [expected-value]
    (assert (= (current-value) (Float. expected-value)))))
