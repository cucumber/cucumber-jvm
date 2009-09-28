require 'cucumber/js_support/backtrace_filter'
require 'forwardable'

module Cucumber
  module JsSupport
    class JsLanguage
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :load_code_file, :step_matches, :begin_scenario, :end_scenario

      def initialize(step_mother)
        @delegate = ::Java::Cuke4dukeInternalJs::JsLanguage.new(self)
      end

      def alias_adverbs(adverbs)
      end

      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "YAY A JAVASCRIPT SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
      end
    end
  end
end

class ::Java::Cuke4dukeInternalJs::JsStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end
