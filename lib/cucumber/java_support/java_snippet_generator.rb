require 'iconv'

module Cucumber
  module JavaSupport
    class JavaSnippetGenerator
      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        escaped = Regexp.escape(step_name).gsub('\ ', ' ').gsub('/', '\/')
        escaped = escaped.gsub(PARAM_PATTERN, ESCAPED_PARAM_PATTERN)
        @iconv ||= Iconv.new('ASCII//IGNORE//TRANSLIT', 'UTF-8')
        method_name = @iconv.iconv(step_name).unpack('U*').select{ |cp| cp < 127 }.pack('U*')
        # Terrible hack for what seems to be a strange JRuby bug (1.4)
        # Without this, method_name is the text of the entire feature (???!!)
        method_name = method_name.split("\n")[-1]
        method_name = method_name.gsub(/"|\s/, '_')
        method_name = method_name.gsub(/_+/, '_')
        method_name = method_name.gsub(/^_/, '')
        method_name = method_name.gsub(/(?:^|_)(.)/) { $1.upcase }
        method_name = method_name[0..0].downcase + (method_name[1..-1] || "")
        method_name += "With" + multiline_arg_class.default_arg_name.capitalize if multiline_arg_class

        n = 0
        args = escaped.scan(ESCAPED_PARAM_PATTERN).map do |a|
          n += 1
          "String arg#{n}"
        end
        args << "cuke4duke.Table #{multiline_arg_class.default_arg_name}" if Cucumber::Ast::Table == multiline_arg_class
        args << "String #{multiline_arg_class.default_arg_name}" if Cucumber::Ast::PyString == multiline_arg_class
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
