use("lib/calculator")

Given(#/^I have entered ({number}\d+) into (?:the|a) calculator$/,
  Calculator pushNumber(number toDecimal))

When(#/^I press ({btn}\w+)$/,
  Calculator calculate(
    case(btn,
      "divide", :/,
      "add", :+
      )))

Then(#/^the current value should be ({expected}[\d.]+)$/,
  Calculator currentValue should == expected toDecimal)
