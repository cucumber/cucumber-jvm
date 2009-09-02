this.metaClass.mixin(cuke4duke.GroovyDsl)

Before(['@newcalc']) {
  calc = new calc.Calculator()
}

Before(['@notused']) {
  throw new RuntimeException("Keep out")
}

Given(~/I have entered (\d+) into the calculator/) { int number ->
  calc = new calc.Calculator()
  calc.push number
}

When(~/I press (\w+)/) { String opname ->
  result = calc."$opname"()
}

Then(~/the stored result should be (.*)/) { double expected -> 
  assert expected == result
}
