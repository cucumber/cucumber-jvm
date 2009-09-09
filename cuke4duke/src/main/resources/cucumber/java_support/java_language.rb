require 'cucumber/java_support/backtrace_filter'
require 'forwardable'

module Cucumber
  module JavaSupport
    class JavaLanguage
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :step_definitions_for, :begin_scenario, :end_scenario

      def initialize(step_mother)
        @delegate = ::Java::Cuke4dukeInternalJava::JavaLanguage.new(self)
      end

      def alias_adverbs(adverbs)
      end

			def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
				name = step_name.strip.gsub(/Undefined step: /, '').chop.gsub(/^"/, '')
        " @#{step_keyword}(\"^#{name}$\")\n public void #{name.gsub(/ /, '')}() { \n }"
      end
    end
  end
end

class ::Java::Cuke4dukeInternalJava::JavaStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end
