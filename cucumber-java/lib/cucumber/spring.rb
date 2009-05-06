require 'cucumber/java'
require 'cucumber/java/version'
require "cucumber-java-#{Cucumber::Java::VERSION::STRING}.jar"
import 'cucumber.internal.StepMother'
import 'cucumber.internal.SpringBasedStepMother'
import 'cucumber.internal.StepDefinition'

module Cucumber
  module PureJava
    def self.extended(base)
      base.instance_eval do
        @__cucumber_java_step_mother = ::Java::CucumberInternal::SpringBasedStepMother.new('steps.xml')
      end
    end
    
    def use_spring_config(configFile)
      @__cucumber_java_step_mother.initContext(configFile)
    end
  end
end
extend(Cucumber::PureJava)

class Java::CucumberInternal::StepDefinition
  include Cucumber::StepDefinitionMethods
  include Cucumber::PureJava::StepDefinitionExtras
end

Exception::CUCUMBER_FILTER_PATTERNS.unshift(/^org\/jruby|^cucumber\/java|^org\/junit|^java\/|^sun\/|^\$_dot_dot_/)
