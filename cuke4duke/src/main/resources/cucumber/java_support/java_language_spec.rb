require 'rubygems'
require 'cucumber'
require 'spec/autorun'

# So we can run on MRI
module Java
  module Cuke4dukeInternalJava
    class JavaStepDefinition
    end
    
    class JavaLanguage
      def initialize(ignore)
      end
    end
  end
end

$:.unshift(File.dirname(__FILE__) + '/../..')
require 'cucumber/java_support/java_language'

module Cucumber
  module JavaSupport
    describe JavaLanguage do
      def unindented(s)
        s.split("\n")[1..-2].join("\n").indent(-10)
      end

      before do
        @java = JavaLanguage.new(nil)
      end
      
      it "should recognise quotes in name and make according regexp" do
        @java.snippet_text('Given', 'A "first" arg').should == unindented(%{
          @Given("^A \"([^\\\"]*)\" arg$")
          @Pending
          public void aFirstArg(String arg1) {
          }
        })
      end

      it "should recognise several quoted words in name and make according regexp and args" do
        @java.snippet_text('Given', 'Æ "first" and "second" arg').should == unindented(%{
          @Given("^Æ \"([^\\\"]*)\" and \"([^\\\"]*)\" arg$")
          @Pending
          public void aEFirstAndSecondArg(String arg1, String arg2) {
          }
        })
      end

      it "should not use quote group when there are no quotes" do
        @java.snippet_text('Given', 'A first arg').should == unindented(%{
          @Given("^A first arg$")
          @Pending
          public void aFirstArg() {
          }
        })
      end

      it "should be helpful with tables" do
        @java.snippet_text('Given', 'A "first" arg', Cucumber::Ast::Table).should == unindented(%{
          @Given("^A \"([^\\\"]*)\" arg$")
          @Pending
          public void aFirstArgWithTable(String arg1, cuke4duke.Table table) {
          }
        })
      end
    end
  end
end