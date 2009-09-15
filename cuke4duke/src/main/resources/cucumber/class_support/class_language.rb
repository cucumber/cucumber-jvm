require 'cucumber/jvm_support/backtrace_filter'
require 'forwardable'

module Cucumber
  module ClassSupport
    class ClassLanguage
      class << self
        attr_accessor :snippet_generator
      end
      
      extend Forwardable
      include ::Cucumber::LanguageSupport::LanguageMethods

      def_delegators :@delegate, :step_definitions_for, :begin_scenario, :end_scenario

      def initialize(step_mother)
        analyzers = [::Java::Cuke4dukeInternalJava::JavaAnalyzer.new]
        @delegate = ::Java::Cuke4dukeInternalJvmclass::ClassLanguage.new(self, analyzers)
      end

      def alias_adverbs(adverbs)
      end

      def activate(analyzer)
        @snippet_generator = analyzer.snippet_generator
      end

      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        @snippet_generator.snippet_text(step_keyword, step_name, multiline_arg_class)
      end
    end
  end
end

class ::Java::Cuke4dukeInternalJava::JavaStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end
class ::Java::Cuke4dukeInternalJava::JavaAnalyzer
  def snippet_generator
    require 'cucumber/java_support/java_snippet_generator'
    Cucumber::JavaSupport::JavaSnippetGenerator.new
  end
end

# Add more .class based programming languages here later