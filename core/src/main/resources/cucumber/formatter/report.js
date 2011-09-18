$(document).ready(function() {
    var formatter = new Cucumber.DOMFormatter($('body'));
    formatter.feature({keyword:'Feature', name:'Generating html report', description: 'It could be useful to have an html report to facilitate documentation reading.'});
    formatter.background({keyword:'Background', name:'Setting up the context', description: 'These steps will be executed before each scenario.'});
    formatter.step({keyword:'Given', name:'I have a background'});
    formatter.step({keyword:'And', name:'I set some context'});
    formatter.scenario({keyword:'Scenario', name: 'Creating a simple report'});
    formatter.step({keyword:'Given', name:'I have a feature'});
    formatter.step({keyword:'When', name:'I format it'});
    formatter.step({keyword:'Then', name:'It should look pretty'});
    formatter.step({keyword:'And', name:'It should show tables', multiline_arg:{type:'table', value: [{cells:['name', 'price'], line:4}, {cells:['milk', '9'], line:5}]}});
    formatter.scenarioOutline({keyword:'Scenario Outline', name: 'Scenario with examples', description:'It should be good to format outlined arguments.'});
    formatter.step({keyword:'Given', name:'I have a <name> which costs <price>'});
    formatter.examples({description:'', name:'Some good examples', keyword:'Examples', line:'10', rows:[{cells:['name', 'price'], line:11}, {cells:['milk', '9'], line:12}, {cells:['bread', '7'], line:13}, {cells:['soap', '5'], line:14}]})
});