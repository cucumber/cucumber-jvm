Given(/^I have entered (\d+) into the calculator$/, function(n) {
  calculator.push(n);
});

When(/^I press (\w+)$/, function(button) {
  this.top = calculator.add();
});

Then(/^the current value should be (\d+)$/, function(value) {
  if(this.top != value) {
    throw "Expected " + value + ", but got " + this.top;
  }
});
