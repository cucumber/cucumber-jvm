require 'cucumber/clj_support/backtrace_filter'
require 'forwardable'

module Cucumber
  module CljSupport
    class CljLanguage
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :load_code_file, :step_matches, :begin_scenario, :end_scenario

      def initialize(step_mother)
        @delegate = ::Java::Cuke4dukeInternalClj::CljLanguage.new(self)
      end

      def alias_adverbs(adverbs)
      end

      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "YAY A CLOJURE SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
      end
    end
  end
end

class ::Java::Cuke4dukeInternalClj::CljStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end
