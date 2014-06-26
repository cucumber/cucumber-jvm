// Hooks
var defineHooks = function(fn, tags, opts) {
    Before(fn, tags, opts);
    After(fn, tags, opts);
};

defineHooks(function() { });
defineHooks(function() { }, "@bellies");
defineHooks(function() { }, [ "@tag1", "@tag2" ]);
defineHooks(function() { }, [ "@tag1", "@tag2" ], { timeout : 300 });
defineHooks(function() { }, [ "@tag1", "@tag2" ], { order : 10 });
defineHooks(function() { }, [ "@tag1", "@tag2" ], { timeout : 600, order : 20 });
