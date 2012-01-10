function assertEquals(expected, actual) {
    if (expected != actual) {
        throw "Expected " + expected + ", but got " + actual;
    }
}

World(function() {

});

Given(/^I have (\d+) cukes in my belly$/, function(n) {
    this.cukes = n;
});

Then(/^there are (\d+) cukes in my belly$/, function(n) {
    assertEquals(n, this.cukes);
});

Then(/^the (.*) contains (.*)$/, function(container, ingredient) {
    assertEquals("glass", container)
});

When(/^I add (.*)$/, function(liquid) {
    assertEquals("milk", liquid);
});
