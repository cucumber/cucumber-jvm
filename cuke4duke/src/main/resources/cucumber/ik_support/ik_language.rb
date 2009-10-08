require 'cucumber/ik_support/backtrace_filter'
require 'forwardable'
module Cucumber
  class IokeException < StandardError
  end

  module IkSupport
    class IkLanguage
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :load_code_file, :step_matches, :begin_scenario, :end_scenario

      def initialize(step_mother)
        @delegate = ::Java::Cuke4dukeInternalIk::IkLanguage.new(self)
      end

      def alias_adverbs(adverbs)
      end

      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "YAY A IOKE SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
      end
    end
  end
end

class ::Java::Cuke4dukeInternalIk::IkStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end
