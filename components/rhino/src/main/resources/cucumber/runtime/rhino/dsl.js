var registerStepDefinition = function(regexp, bodyFunc) {
  var argumentsFromFunc = function(stepName) {
    var match = regexp.exec(stepName);
    if(match) {
      var arguments = new Packages.java.util.ArrayList();
      var s = match[0];
      var offset = 0;
      for(i = 1; i < match.length; i++) {
        var arg = match[i];
        var offset = s.indexOf(arg, offset);
        arguments.add(new Packages.gherkin.formatter.Argument(offset, arg));
      }
      return arguments;
    } else {
      return null;
    }
  };
  jsBackend.addStepDefinition(this, bodyFunc, argumentsFromFunc);
};

var Given = registerStepDefinition;
var When = registerStepDefinition;
var Then = registerStepDefinition;

var World = function(func) {
    // TODO: do this properly
    func();
}