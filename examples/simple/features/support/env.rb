# Cucumber Java depends on PicoContainer to instantiate classes with Step definitions
require File.expand_path('~/.m2/repository/org/picocontainer/picocontainer/2.8/picocontainer-2.8.jar')

# Load Cucumber Java support
require 'cucumber/java'

# Load JUnit - our Step definitions use it for assertions
require File.expand_path('~/.m2/repository/junit/junit/4.4/junit-4.4.jar')

# Load our step definitions
require File.expand_path(File.dirname(__FILE__) + '/../../target/cucumber-simple-example-0.2.jar')

# Register our steps definitions.
import 'simple.StuffSteps'
register_steps(StuffSteps)
