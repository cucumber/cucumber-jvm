calculator = {
  push: function(n) {
    if(!this.stack) this.stack = [];
    this.stack.push(n);
  },
  
  divide: function() {
    return this.stack[0] / this.stack[1];
  }
}

Given(/^I have entered (\d+) into the calculator$/, function(n) {
  calculator.push(parseFloat(n));
});

When(/^I press (\w+)$/, function(button) {
  this.top = calculator.divide();
});

Then(/^the current value should be (.*)$/, function(value) {
  value = parseFloat(value);
  if(this.top != value) {
    throw "Expected " + value + ", but got " + this.top;
  }
});
