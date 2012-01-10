_n = nil
_what = nil

Given(#/^I have ({n}\d+) "({what}[^"]*)" in my belly$/,
  _n = n
  _what = what
)

Then(#/^there are ({n}\d+) "({what}[^"]*)" in my belly$/,
  _n should == n
  _what should == what
)
