module Cucumber
  module GroovySupport
    class GroovyLanguage
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :step_mother, :load_step_def_file, :begin_scenario, :end_scenario

      def initialize(step_mother)
        @delegate = ::Java::Cuke4dukeInternalGroovy::GroovyLanguage.new(step_mother)
      end

      def alias_adverbs(adverbs)
      end

      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "YAY A GROOVY SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
      end
    end
  end
end

class ::Java::Cuke4dukeInternalGroovy::GroovyLanguage
  include ::Cucumber::LanguageSupport::LanguageMethods
end

class ::Java::Cuke4dukeInternalGroovy::GroovyStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end

class ::Java::Cuke4dukeInternalGroovy::GroovyHook
  include ::Cucumber::LanguageSupport::HookMethods
end
