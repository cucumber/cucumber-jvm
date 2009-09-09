require 'cucumber/java_support/backtrace_filter'
require 'forwardable'
require 'iconv'

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
        escaped = Regexp.escape(step_name).gsub('\ ', ' ').gsub('/', '\/')
        escaped = escaped.gsub(PARAM_PATTERN, ESCAPED_PARAM_PATTERN)

        method_name = Iconv.iconv('ascii//ignore//translit', 'utf-8', step_name).to_s
        method_name = method_name.gsub(/"|\s/, '_')
        method_name = method_name.gsub(/_+/, '_')
        method_name = method_name.gsub(/(?:^|_)(.)/) { $1.upcase }
        method_name = method_name[0..0].downcase + (method_name[1..-1] || "")
        method_name += "With" + multiline_arg_class.default_arg_name.capitalize if multiline_arg_class

        n = 0
        args = escaped.scan(ESCAPED_PARAM_PATTERN).map do |a|
          n += 1
          "String arg#{n}"
        end
        args << "cuke4duke.Table #{multiline_arg_class.default_arg_name}" unless multiline_arg_class.nil?
        arg_string = args.join(", ")

        %{@#{step_keyword}("^#{escaped}$")\n} +
        %{@Pending\n} +
        %{public void #{method_name}(#{arg_string}) {\n}}

        # step_description = step_name.gsub(/"/, '\"')
        # "@#{step_keyword}(\"^#{step_description}$\") \npublic void #{step_name.gsub(/"/, '')}(#{multiline_arg_class}) { \n} "
      end

      private

      PARAM_PATTERN = /"([^\"]*)"/
      ESCAPED_PARAM_PATTERN = '"([^\\"]*)"'

    end
  end
end

class ::Java::Cuke4dukeInternalJava::JavaStepDefinition
  include ::Cucumber::LanguageSupport::StepDefinitionMethods
end