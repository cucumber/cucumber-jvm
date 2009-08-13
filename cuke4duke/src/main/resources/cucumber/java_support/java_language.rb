require 'forwardable'

module Cucumber
  module JavaSupport
    class JavaLanguage
      extend Forwardable
      include ::Cucumber::LanguageMethods

      def_delegators :@delegate, :load_step_def_file, :new_world, :nil_world

      def initialize(step_mother)
        @delegate = ::Java::Cuke4dukeInternalJava::JavaLanguage.new(step_mother)
      end
      
      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "YAY A JAVA SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
      end
    end
  end
end

class ::Java::Cuke4dukeInternalJava::JavaLanguage
  include ::Cucumber::LanguageMethods
end

class ::Java::Cuke4dukeInternalJava::JavaStepDefinition
  include ::Cucumber::StepDefinitionMethods
end

class ::Java::Cuke4dukeInternalJava::JavaHook
  include ::Cucumber::HookMethods
end
