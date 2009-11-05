(add-classpath (str "file://" (.. (java.io.File. *file*) getParentFile getParentFile getParent) "/lib/"))
(use 'calculator)
;; (use 'clojure.contrib.test-is)

(Before
  (dosync (ref-set numbers [])))

(Given #"^I have entered ([\d.]+) into the calculator$"
  #(push-number (Float. %1)))

(When #"^I press (\w+)$"
  #(calculate ({"divide" / "add" +} %)))

;; Verbose style
(Then #"^the current value should be ([\d.]+)$"
  (fn [expected]
;;    (is (= (current-value) (Float. expected)))))
    (assert (= (current-value) (Float. expected)))))
