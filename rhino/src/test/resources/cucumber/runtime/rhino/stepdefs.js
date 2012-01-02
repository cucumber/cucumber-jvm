function assertEquals(expected, actual) {
    if (expected != actual) {
        throw "Expected " + expected + ", but got " + actual;
    }
}

World(function() {

});

Given(/^I have (\d+) "([^"]*)" in my belly$/, function(n, what) {
    this.n = n;
    this.what = what;
});

Then(/^there are (\d+) "([^"]*)" in my belly$/, function(n, what) {
    assertEquals(n, this.n);
    assertEquals(what, this.what);
});
