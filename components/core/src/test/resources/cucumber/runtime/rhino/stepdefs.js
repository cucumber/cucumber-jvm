World(function() {

});

Given(/^I have (\d+) cukes in my belly$/, function(n) {
    this.cukes = n;
});

Then(/^there are (\d+) cukes in my belly$/, function(n) {
    if (this.cukes != n) {
        throw "Expected " + n + ", but got " + this.cukes;
    }
});
