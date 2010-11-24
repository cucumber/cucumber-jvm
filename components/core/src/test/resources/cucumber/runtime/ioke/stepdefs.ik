cukes = nil

Given(#/^I have ({n}\d+) cukes in my belly$/,
  cukes = n
)

Then(#/^there are ({n}\d+) cukes in my belly$/,
  cukes should == n
)
