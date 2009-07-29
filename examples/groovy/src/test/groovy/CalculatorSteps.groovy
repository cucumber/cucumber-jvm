import cuke4duke.*

@Before("")
def create(scenario) {
  calc = new calc.Calculator()
}

@Given(/I have entered (\d+) into the calculator/)
def enter(number) {
  calc.push number as int
}

@When(/I press (\w+)/)
def op(opname) {
  result = calc."$opname"()
}

@Then(/the stored result should be (.*)/)
def checkResult(expected) {
  assert expected == result.toString()
}