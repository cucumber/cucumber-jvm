require 'rubygems'
require 'cucumber'
require 'spec/autorun'

$:.unshift(File.dirname(__FILE__) + '/../..')
require 'cucumber/java_support/java_snippet_generator'

module Cucumber
  module JavaSupport
    describe JavaSnippetGenerator do
      def unindented(s)
        s.split("\n")[1..-2].join("\n").indent(-10)
      end

      before do
        @generator = JavaSnippetGenerator.new
      end
      
      it "should recognise quotes in name and make according regexp" do
        @generator.snippet_text('Given', 'A "first" arg').should == unindented(%{
          @Given("^A \\\"([^\\\"]*)\\\" arg$")
          @Pending
          public void aFirstArg(String arg1) {
          }
        })
      end

      it "should recognise several quoted words in name and make according regexp and args" do
        @generator.snippet_text('Given', 'Æ "first" and "second" arg').should == unindented(%{
          @Given("^Æ \\\"([^\\\"]*)\\\" and \\\"([^\\\"]*)\\\" arg$")
          @Pending
          public void aEFirstAndSecondArg(String arg1, String arg2) {
          }
        })
      end

      it "should not use quote group when there are no quotes" do
        @generator.snippet_text('Given', 'A first arg').should == unindented(%{
          @Given("^A first arg$")
          @Pending
          public void aFirstArg() {
          }
        })
      end

      it "should be helpful with tables" do
        @generator.snippet_text('Given', 'A "first" arg', Cucumber::Ast::Table).should == unindented(%{
          @Given("^A \\\"([^\\\"]*)\\\" arg$")
          @Pending
          public void aFirstArgWithTable(String arg1, cuke4duke.Table table) {
          }
        })
      end

      it "should be helpful with multiline strings" do
        @generator.snippet_text('Given', 'A "first" arg', Cucumber::Ast::PyString).should == unindented(%{
          @Given("^A \\\"([^\\\"]*)\\\" arg$")
          @Pending
          public void aFirstArgWithString(String arg1, String string) {
          }
        })
      end
    end
  end
end