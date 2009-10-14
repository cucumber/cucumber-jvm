require 'cuke4duke/language_proxy'

Cuke4Duke.cuke4!('ik')

module Cucumber
  class IokeException < StandardError
  end
 
  module IkSupport
    class IkLanguage
      def snippet_text(step_keyword, step_name, multiline_arg_class = nil)
        "#{step_keyword}(#/^#{step_name}$/,\n  pending\n)\n"
      end
    end
  end
end