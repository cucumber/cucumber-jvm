module Cucumber
  module GroovySupport
    class GroovyLanguage
      extend Forwardable
      include ::Cucumber::LanguageMethods

      def_delegators :@delegate, :step_mother, :load_step_def_file, :begin_scenario, :end_scenario

      def initialize(step_mother, adverbs)
        @delegate = ::Java::Cuke4dukeInternalGroovy::GroovyLanguage.new(step_mother, adverbs)
      end

      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "YAY A GROOVY SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
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
