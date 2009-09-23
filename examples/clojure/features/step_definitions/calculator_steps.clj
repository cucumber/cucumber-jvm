(println "BEFORE STEPDEF")

(then "I should have (.*) cukes in my belly"
  (fn [cukes] 
    (println "Hello," cukes)))
    
(println "AFTER STEPDEF")
