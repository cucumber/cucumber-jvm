var stepdef = function(regexp, code) {
  jsLanguage.addStepDefinition(regexp, code);
};

var Given = stepdef;
var When = stepdef;
var Then = stepdef;