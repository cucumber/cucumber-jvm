module Cucumber
  module GroovySupport
    class GroovyLanguage
      def self.new(step_mother)
        ::Java::Cuke4dukeInternalGroovy::GroovyLanguage.new(step_mother)
      end
    end
  end
end

class ::Java::Cuke4dukeInternalGroovy::GroovyLanguage
  include ::Cucumber::LanguageMethods
end

class ::Java::Cuke4dukeInternalGroovy::GroovyStepDefinition
  include ::Cucumber::StepDefinitionMethods
end

class ::Java::Cuke4dukeInternalGroovy::GroovyHook
  include ::Cucumber::HookMethods
end
