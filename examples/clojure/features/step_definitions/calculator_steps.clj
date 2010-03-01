(use 'calculator)
;; We're not using test-is yet, since failures are just printed - nothing is raised.
;; (use 'clojure.contrib.test-is)

;; Just test that it works
(After)

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
