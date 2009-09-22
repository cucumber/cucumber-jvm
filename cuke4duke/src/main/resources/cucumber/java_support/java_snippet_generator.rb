require 'iconv'

module Cucumber
  module JavaSupport
    class JavaSnippetGenerator
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
      end

      private

      PARAM_PATTERN = /"([^\"]*)"/
      ESCAPED_PARAM_PATTERN = '\\"([^\\"]*)\\"'

    end
  end
end
