cucumber_support = File.expand_path(File.dirname(__FILE__) + '/../../../../target/cucumber-support-0.2.jar')
require cucumber_support
pico = File.expand_path('~/.m2/repository/org/picocontainer/picocontainer/2.8/picocontainer-2.8.jar')
require pico
junit = File.expand_path('~/.m2/repository/junit/junit/4.4/junit-4.4.jar')
require junit

import 'cucumber.internal.StepDefinition'
import 'cucumber.internal.StepMother'
import 'cucumber.Table'

module Cucumber
  module Ast
    class Table
      include Java::Cucumber::Table
    end
  end
  
  module PureJava
    module StepDefinitionExtras # TODO: Move to Java
      def regexp
        Regexp.new(getRegexpString)
      end
    end
    
    def self.extended(base)
      base.instance_eval do
        @__cucumber_java_step_mother = Java::CucumberInternal::StepMother.new
      end
    end

    def register_steps(steps_class)
      @__cucumber_java_step_mother.add(steps_class)
    end

    def new_world!
      @__cucumber_java_step_mother.newWorld
    end
    
    def step_definitions
      @__cucumber_java_step_mother.getStepDefinitions
    end
  end
end
extend(Cucumber::PureJava)

class Java::CucumberInternal::StepDefinition
  include Cucumber::StepDefinitionMethods
  include Cucumber::PureJava::StepDefinitionExtras
end

project_code = File.expand_path(File.dirname(__FILE__) + '/../../target/cucumber-simple-example-0.2.jar')
require project_code

import 'simple.StuffSteps'
register_steps(StuffSteps)
