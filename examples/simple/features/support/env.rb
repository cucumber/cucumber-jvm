#require File.expand_path('~/.m2/repository/org/picocontainer/picocontainer/2.8/picocontainer-2.8.jar')
#require File.expand_path('~/.m2/repository/junit/junit/4.4/junit-4.4.jar')
#require File.expand_path(File.dirname(__FILE__) + '/../../target/cucumber-simple-example-1.0.0.jar')

require 'cucumber/java'

register_steps(Java::simple.StuffSteps)
