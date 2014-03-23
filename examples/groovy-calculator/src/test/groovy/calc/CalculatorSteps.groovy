package calc

// Add functions to register hooks and steps to this script.
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

// Define a world that represents the test environment.
// Hooks can set up and tear down the environment and steps
// can change its state, e.g. store values used by later steps.
class CustomWorld {
    def result

    String customMethod() {
        "foo"
    }
}

// Create a fresh new world object as the test environment for each scenario.
// Hooks and steps will belong to this object so can access its properties
// and methods directly.
World {
    new CustomWorld()
}

// This closure gets run before each scenario
// and has direct access to the new world object
// but can also make use of script variables.
Before() {
    assert "foo" == customMethod()
    calc = new Calculator() // belongs to this script
}

// Register another that also gets run before each scenario tagged with @notused.
Before("@notused") {
    throw new RuntimeException("Never happens")
}

// Register another that also gets run before each scenario tagged with
// (@notused or @important) and @alsonotused.
Before("@notused,@important", "@alsonotused") {
    throw new RuntimeException("Never happens")
}

// Register step definition using Groovy syntax for regex patterns.
// If you use slashes to quote your regexes, you don't have to escape backslashes. 
// Any Given/When/Then function can be used, the name is just to indicate the kind of step.
Given(~/I have entered (\d+) into .* calculator/) { int number ->
    calc.push number
}

// Remember to still include "->" if there are no parameters.
Given(~/\d+ into the/) {->
    throw new RuntimeException("should never get here since we're running with --guess")
}

// This step calls a Calculator function specified in the step
// and saves the result in the current world object.
When(~/I press (\w+)/) { String opname ->
    result = calc."$opname"()
}

// Use the world object to get any result from a previous step.
// The expected value in the step is converted to the required type.
Then(~/the stored result should be (.*)/) { double expected ->
    assert expected == result
}
