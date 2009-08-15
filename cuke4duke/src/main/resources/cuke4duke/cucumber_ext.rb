require 'java'
import 'cuke4duke.Table'
import 'cuke4duke.internal.language.StepMother'

module Cucumber
  class StepMother
    include Java.cuke4duke.internal.language.StepMother
  end

  module Ast
    class Table
      include Java.cuke4duke.Table
      
      def diffLists(table, options={})
        diff!(table, opts(options))
      end

      def diffHashes(table, options={})
        diff!(table, opts(options))
      end

    private

      def opts(options)
        return options if Hash === options
        opts = {}
        options.each{|k, v| opts[k.to_sym] = v}
        opts
      end
    end
  end
end