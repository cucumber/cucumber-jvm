use("lib/calculator")

Given(#/^I have entered ({number}\d+) into (.*) calculator$/,
  Calculator pushNumber(number))

When(#/^I press ({btn}\w+)$/,
  Calculator calculate(
    case(btn,
      "divide", :/,
      "add", :+
      )))

Then(#/^the current value should be ({expected}[\d.]+)$/,
  Calculator currentValue should == expected asNumber)
