require File.expand_path('~/.m2/repository/org/picocontainer/picocontainer/2.8/picocontainer-2.8.jar')
require File.expand_path('~/.m2/repository/junit/junit/4.4/junit-4.4.jar')
$CLASSPATH << File.dirname(__FILE__) + '/../../target/classes'

require 'cucumber/java'

register_steps(Java::simple.StuffSteps)
