$(document).ready(function() {
    var formatter = new Cucumber.DOMFormatter($('body'));
    formatter.uri('report.feature');
    formatter.feature({keyword:'Feature', name:'Generating html report', line:2, description: 'It could be useful to have an html report to facilitate documentation reading.'});
    formatter.background({keyword:'Background', name:'Setting up the context', line:3, description: 'These steps will be executed before each scenario.'});
    formatter.step({keyword:'Given', name:'I have a background', line:4});
    formatter.step({keyword:'And', name:'I set some context', line: 5});
    formatter.scenario({keyword:'Scenario', name: 'Creating a simple report', line: 6});
    formatter.step({keyword:'Given', name:'I have a feature', line: 7});
    formatter.step({keyword:'When', name:'I format it', line: 8});
    formatter.step({keyword:'Then', name:'It should look pretty', line: 9});
    formatter.step({keyword:'And', name:'It should show tables', line: 10, multiline_arg:{type:'table', value: [{cells:['name', 'price'], line: 11}, {cells:['milk', '9'], line: 12}]}});
    formatter.scenarioOutline({keyword:'Scenario Outline', name: 'Scenario with examples', description:'It should be good to format outlined arguments.', line: 13});
    formatter.step({keyword:'Given', name:'I have a <name> which costs <price>', line: 14});
    formatter.examples({description:'', name:'Some good examples', keyword:'Examples', line: 15, rows:[{cells:['name', 'price'], line:16}, {cells:['milk', '9'], line:17}, {cells:['bread', '7'], line:18}, {cells:['soap', '5'], line:19}]})
    
    var reporter = new Cucumber.Reporter();
    reporter.match({uri:'report.feature', step:{line:4}});
    reporter.result({status:'passed', duration: 0});
    reporter.match({uri:'report.feature', step:{line:5}});
    reporter.result({status:'passed', duration: 0});
    
    reporter.match({uri:'report.feature', step:{line:7}});
    reporter.result({status:'passed', duration: 0});
    reporter.match({uri:'report.feature', step:{line:8}});
    reporter.result({status:'failed', error_message:'something went wrong...', duration: 0});
    reporter.match({uri:'report.feature', step:{line:9}});
    reporter.result({status:'skipped', duration: 0});
    reporter.match({uri:'report.feature', step:{line:10}});
    reporter.result({status:'passed', duration: 0});
    
    reporter.match({uri:'report.feature', step:{line:16}});
    reporter.result({status:'passed', duration: 0});
    reporter.match({uri:'report.feature', step:{line:17}});
    reporter.result({status:'passed', duration: 0});
    reporter.match({uri:'report.feature', step:{line:18}});
    reporter.result({status:'passed', duration: 0});
    reporter.match({uri:'report.feature', step:{line:19}});
    reporter.result({status:'failed', error_message:'I didn\'t do it.', duration: 0});
});