import 'cuke4duke.Table'

module Cucumber
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
