package cucumber.runtime.groovy

this.metaClass.mixin(cucumber.runtime.groovy.Dsl)

Given(~"I have (\\d+) cukes in my belly") { String cukes ->
  System.out.println("My cukes:" + cukes);
}
