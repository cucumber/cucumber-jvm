import 'cuke4duke.Table'

module Cucumber
  module Ast
    class Table
      include Java.cuke4duke.Table
      
      def diffLists(list_list, options={})
        diff!(lists_to_arrays(list_list), opts(options))
      end

      def diffHashes(map_list, options={})
        diff!(maps_to_hashes(map_list), opts(options))
      end

      def convertColumn(column, converter)
        map_column!(column) { |value| converter.convert(value) }
      end

      def convertHeaders(converter)
        map_headers! { |header| converter.convert(header) }
      end
      
    private

      def opts(options)
        return options if Hash === options
        opts = {}
        options.each{|k, v| opts[k.to_sym] = v}
        opts
      end

      def lists_to_arrays(list_list)
        list_list.map{|list| list.map{|e| e}}
      end

      def maps_to_hashes(map_list)
        map_list.collect{|map| map_to_hash(map)}
      end

      def map_to_hash(map)
        hash = {}
        map.each{|entry| hash[entry[0]] = entry[1]}
        hash
      end
    end
  end
end
