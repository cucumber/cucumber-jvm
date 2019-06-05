$(document).ready(function() {
  var formatter = new CucumberHTML.DOMFormatter($('.cucumber-report'));
var N = document.location.hash ? parseInt(document.location.hash.substring(1)) : 1;
var start = new Date().getTime();
for(var n = 0; n < N; n++) {
  formatter.uri('report.feature');
  formatter.feature({
    comments: [
      {value: "# A comment"},
      {value: "# Another comment"}
    ],
    keyword:'Feature',
    name:'Generating html report',
    description: 'It could be useful to have an html report to facilitate documentation reading.\n\nEspecially when the whitespace formatting is preserved',
    line:2
  });

  formatter.background({
    comments: [
      {value: "# Background comment"}
    ],
    keyword:'Background',
    name:'Setting up the context',
    line:3,
    description: 'These steps will be executed before each scenario.'
  });
  formatter.write('Output from before hook');
  formatter.embedding('text/plain', 'Text embedding from before hook');
  formatter.step({keyword:'Given ', name:'I have a background', line:4});
  formatter.step({keyword:'And ', name:'I set some context', line: 5});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'passed', duration: 0});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'passed', duration: 0});

  formatter.before({status: 'passed', duration: 668816288});
  formatter.scenario({"tags":[{"name":"@foo","line":3},{"name":"@bar","line":4},{"name":"@doh","line":5}], keyword:'Scenario', name: 'Creating a simple report', line: 6});
  formatter.step({comments: [
    {value: "# Step comment 1"},
    {value: "# Step comment 2"}
  ],keyword:'Given ', name:'I have a feature', line: 7, doc_string:{value: "A\ndoc string\non several lines", content_type:"text/plain", line:8}});
  formatter.step({keyword:'When ', name:'I format it', line: 11});
  formatter.step({keyword:'Then ', name:'It should look pretty', line: 12});
  formatter.step({keyword:'And ', name:'It should show tables', line: 13, rows: [{cells:['name', 'price'], line: 14}, {cells:['milk', '9'], line: 15}]});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'passed', duration: 0});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'failed', error_message:'something went wrong...', duration: 0});
  formatter.embedding('image/png', 'bubble_256x256.png');
  formatter.match({uri:'report.feature'});
  formatter.result({status:'undefined', duration: 0});
  formatter.embedding('text/plain', 'Look at this video');
  formatter.embedding('video/mp4', 'http://www.808.dk/pics/video/gizmo.mp4');
  formatter.embedding('text/plain', 'Look at this multi-line embedding\nReal fancy');
  formatter.write('What a nice helicopter');
  formatter.match({uri:'report.feature'});
  formatter.result({status:'skipped', duration: 0});
  formatter.after({status: 'passed', duration: 668816288});

  formatter.scenarioOutline({keyword:'Scenario Outline', name: 'Scenario with examples', description:'It should be good to format outlined arguments.', line: 16});
  formatter.step({keyword:'Given ', name:'I have a <name> which costs <price>', line: 17});
  formatter.examples({description:'', name:'Some good examples', keyword:'Examples', line: 18, rows:[{cells:['name', 'price'], line:19}, {cells:['milk', '9'], line:20}, {cells:['bread', '7'], line:21}, {cells:['soap', '5'], line:22}]});
  formatter.before({status: 'passed', duration: 668816288});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'passed', duration: 0});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'passed', duration: 0});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'failed', error_message:'I didn\'t do it.', duration: 0});
  formatter.after({status: 'failed', duration: 668816288, "error_message": 'com.example.MyDodgyException: Widget underflow\r\n\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:264)\r\n\tat com.example.WidgetFurbicator.furbicateWidgets(WidgetFurbicator.java:678)'});

  formatter.scenario({"tags":[{"name":"@stephooks","line":24}], keyword:'Scenario', name: 'Scenario with step hooks', line: 25});
  formatter.before({status: 'passed', duration: 668816288});
  formatter.beforestep({status: 'passed', duration: 668816288});
  formatter.step({keyword:'Given ', name:'step 1', line: 26});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'passed', duration: 0});
  formatter.afterstep({status: 'passed', duration: 668816288});
  formatter.beforestep({status: 'failed', duration: 668816288, "error_message": 'com.example.MyDodgyException: Widget underflow\r\n\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:264)\r\n\tat com.example.StepDefinitions.beforeStepHook()'});
  formatter.step({keyword:'When ', name:'step 2', line: 27});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'skipped', duration: 0});
  formatter.afterstep({status: 'failed', duration: 668816288, "error_message": 'com.example.MyDodgyException: Widget underflow\r\n\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:264)\r\n\tat com.example.StepDefinitions.afterStepHook()'});
  formatter.beforestep({status: 'skipped', duration: 0});
  formatter.step({keyword:'Then ', name:'step 3', line: 28});
  formatter.match({uri:'report.feature'});
  formatter.result({status:'skipped', duration: 0});
  formatter.afterstep({status: 'skipped', duration: 0});
  formatter.after({status: 'passed', duration: 668816288});
}
console.log('Rendered %s features in %s ms', N, new Date().getTime() - start);

});
