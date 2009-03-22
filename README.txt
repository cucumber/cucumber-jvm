History.txt entry for Cucumber when this is complete:

This version also brings Cucumber even closer to Java. Although it has been possible to
run Cucumber on JRuby since v0.1.11, it has required that step definitions be defined
in Ruby code. This has been a barrier for many Java developers who don't know Ruby.

With this version you can write step definitions in pure Java code! Java step definitions
are implemented simply by subclassing org.jbehave.scenario.steps.Steps. Example:

  package super.duper;

  public class MyJavaSteps extends Steps {
      @Given("I have %count cucumbers in my belly")
      public void cucumbersInTheBelly(int count) {
          // talk to the Belly class
      }

      @Then("I should not be hungry for %n hours")
      public void shouldNotBeHungry(int n) {
          // Make assertions with JUnit or Hamcrest
      }
  }

The only Ruby code you have to write is a little wiring in your env.rb file:

  require 'cucumber/jbehave'
  JBehave(super.duper.MyJavaSteps.new)

You might be wondering what this brings over just using the whole JBehave tool
standalone. Well, it gives you access to the Gherkin language in your features,
which is a richer DSL than JBehave's language. -Over 20 spoken languages, Tables, 
Scenario Outlines, the rich command line, the nice output format and everything 
pure Ruby users have been enjoying for a while.

