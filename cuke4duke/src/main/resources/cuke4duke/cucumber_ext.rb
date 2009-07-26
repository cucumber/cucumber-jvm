require 'java'
import 'cuke4duke.Table'

module Cucumber
  module Ast
    class Table
      include Java.cuke4duke.Table
    end
  end
end