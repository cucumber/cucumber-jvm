require 'cuke4duke/language_proxy'

Cuke4Duke.cuke4!('class')

module Cucumber
  module ClassSupport
    class ClassLanguage
      class << self
        attr_reader :snippet_generator, :analyzers

        def add_analyzers(analyzer)
          @analyzers ||= []
          @analyzers << analyzer
          @snippet_generator = analyzer.snippet_generator
        end
      end
      
      def initialize(step_mother)
        @delegate = Java.cuke4duke.internal.jvmclass.ClassLanguage.new(self, Java.cuke4duke.spi.jruby.JRubyExceptionFactory.new, step_mother, self.class.analyzers)
      end

      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        self.class.snippet_generator.snippet_text(step_keyword, step_name, multiline_arg_class)
      end
    end
  end
end

require 'cucumber/java_support/java_analyzer'
require 'cucumber/scala_support/scala_analyzer'