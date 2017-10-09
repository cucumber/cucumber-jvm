var registerStepDefinition = function(regexp, bodyFunc) {
    var argumentsFromFunc = function(stepName) {
        var match = regexp.exec(stepName);
        if (match) {
            var arguments = new Packages.java.util.ArrayList();
            var s = match[0];
            var offset = 0;
            for (var i = 1; i < match.length; i++) {
                var arg = match[i];
                offset = s.indexOf(arg, offset);
                arguments.add(new Packages.cucumber.runtime.Argument(offset, arg));
            }
            return arguments;
        } else {
            return null;
        }
    };
    jsBackend.addStepDefinition(this, regexp, bodyFunc, argumentsFromFunc);
};

var registerHookDefinition = function(addHookFn, fn, tags, opts) {
    if (tags) {
        // if tags is a string, convert it into an array
        if (typeof tags === "string") {
            tags = [ tags ];
        }
    } else {
        tags = [];
    }

    tags = tags instanceof Array ? tags : [];
    opts = opts || {};

    var order = opts.order || 1000;
    var timeout = opts.timeout || 0;
    addHookFn.call(jsBackend, fn, tags, order, timeout);
};

Before = function(fn, tags, opts) {
    registerHookDefinition(jsBackend.addBeforeHook, fn, tags, opts);
};

After = function(fn, tags, opts) {
    registerHookDefinition(jsBackend.addAfterHook, fn, tags, opts);
};

var Given = registerStepDefinition;
var When = registerStepDefinition;
var Then = registerStepDefinition;

var World = function(buildFn, disposeFn) {
    jsBackend.registerWorld(buildFn, disposeFn || null);
};
