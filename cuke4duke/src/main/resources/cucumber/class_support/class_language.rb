require 'forwardable'

module Cucumber
  module ClassSupport
    class ClassLanguage
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :step_definitions_for, :begin_scenario, :end_scenario

      def initialize(step_mother)
        @delegate = ::Java::Cuke4dukeInternalJvmclass::ClassLanguage.new(self)
      end

      def alias_adverbs(adverbs)
      end
    end
  end
end

class ::Java::Cuke4dukeInternalJava::JavaStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end

# Add more .class based programming languages here later