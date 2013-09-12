function assertEquals(expected, actual) {
    if (expected != actual) {
        throw "Expected " + expected + ", but got " + actual;
    }
}

function assertContains(expectedVal, array) {
    for ( var i = 0; i < array.length; i++) {
        if (array[i] == expectedVal) return;
    }
    throw "Expected array containing " + expectedVal + ", but got " + array;
}

World(function() {

});

// Hooks
Before(function() {s
    this.belliesMissing = [];
});

After(function() {
    delete this.belliesMissing;
});

Before(function() {
    this.bellies = {};
}, "@bellies");

After(function() {
    delete this.bellies;
}, "@bellies");

// Steps
Given(/^I have (\d+) "([^"]*)" in my belly$/, function(n, what) {
    this.n = n;
    this.what = what;
});

Then(/^there are (\d+) "([^"]*)" in my belly$/, function(n, what) {
    assertEquals(n, this.n);
    assertEquals(what, this.what);
});

Given(/^(\w+) has (\d+) "([^"]*)" in his belly$/, function(bellyOwner, n, what) {
    if (this.bellies) {
        this.bellies[bellyOwner] = {
            n : n,
            what : what
        };
    } else {
        this.belliesMissing.push(bellyOwner);
    }
});

Then(/^there are (\d+) "([^"]*)" in the belly of (\w+)$/, function(n, what, bellyOwner) {
    assertEquals(n, this.bellies[bellyOwner].n);
    assertEquals(what, this.bellies[bellyOwner].what);
});

Then(/^I wake up and there is no (\w+)$/, function(bellyOwner) {
    assertContains(bellyOwner, this.belliesMissing);
});