# This file is loaded before Cucumber is loaded.
require 'java'
import 'cuke4duke.Table'
import 'cuke4duke.internal.language.StepMother'

# Workaround to make the java code have access to the same Ruby
# interpreter as the one that is used to run this script.
# org.jruby.Main uses a different instance than what 
# org.jruby.Ruby.getGlobalRuntime() returns. It might be considered
# a JRuby bug.
Java.cuke4duke.internal.JRuby.setRuntime(JRuby.runtime)

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
        diff!(maps_to_hashes(table), opts(options))
      end

    private

      def opts(options)
        return options if Hash === options
        opts = {}
        options.each{|k, v| opts[k.to_sym] = v}
        opts
      end

      def maps_to_hashes(maps)
        maps.collect{|map| map_to_hash(map)}
      end

      def map_to_hash(map)
        hash = {}
        map.each{|entry| hash[entry[0]] = entry[1]}
        hash
      end
    end
  end
end
