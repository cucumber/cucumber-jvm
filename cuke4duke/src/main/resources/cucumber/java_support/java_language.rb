require 'forwardable'

module Cucumber
  module JavaSupport
    class JavaLanguage
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :step_mother, :load_step_def_file, :begin_scenario, :end_scenario

      def initialize(step_mother, adverbs)
        @delegate = ::Java::Cuke4dukeInternalJava::JavaLanguage.new(step_mother, adverbs)
      end
      
      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "YAY A JAVA SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
      end
    end
  end
end

class ::Java::Cuke4dukeInternalJava::JavaLanguage
  include ::Cucumber::LanguageSupport::LanguageMethods
end

class ::Java::Cuke4dukeInternalJava::JavaStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end

class ::Java::Cuke4dukeInternalJava::JavaHook
  include ::Cucumber::LanguageSupport::HookMethods
end
