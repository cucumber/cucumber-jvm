// Hooks
Before(function() {
    // this must fail with timeout
    Packages.java.lang.Thread.sleep(200);
}, [], { timeout : 100 });