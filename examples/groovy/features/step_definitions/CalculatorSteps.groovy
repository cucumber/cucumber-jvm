this.metaClass.mixin(cuke4duke.GroovyDsl)

Before(['@newcalc']) {
  calc = new calc.Calculator()
}

Before(['@notused']) {
  throw new RuntimeException("Keep out")
}

Given(~/I have entered (\d+) into (.*) calculator/) { int number, String ignore ->
  calc = new calc.Calculator()
  calc.push number
}

Given(~/(\d+) into the/) { ->
  throw new RuntimeException("should never get here since we're running with --guess")
}

When(~/I press (\w+)/) { String opname ->
  result = calc."$opname"()
}

Then(~/the stored result should be (.*)/) { double expected -> 
  assert expected == result
}
