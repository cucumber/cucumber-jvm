package calc

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

class CustomWorld {
    String customMethod() {
        "foo"
    }
}

World {
    new CustomWorld()
}

Before() {
    assert "foo" == customMethod()
    calc = new Calculator()
}

Before("@notused") {
    throw new RuntimeException("Never happens")
}

Before("@notused,@important", "@alsonotused") {
    throw new RuntimeException("Never happens")
}

Given(~"I have entered (\\d+) into (.*) calculator") { int number, String ignore ->
    calc.push number
}

Given(~"(\\d+) into the") {->
    throw new RuntimeException("should never get here since we're running with --guess")
}

When(~"I press (\\w+)") { String opname ->
    result = calc."$opname"()
}

Then(~"the stored result should be (.*)") { double expected ->
    assert expected == result
}
