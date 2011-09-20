var Cucumber = {};

//See http://www.w3.org/TR/html4/types.html#type-id
/**
 * Generates an unique id so we can find statements and mark them after execution
 */
Cucumber.encodeId = function(uri, line) {
    return 'id_' + uri.replace(/(:|\.|\/)/g,'_') + '_' + line;
}

Cucumber.DOMFormatter = function(rootNode) {
    var rootNode = rootNode;
    var featureElementsNode;
    var scenarioElementsNode;
    var currentNode;
    var currentUri;

    this.uri = function(uri) {
        currentUri = uri;
    }
    
    this.feature = function(feature) {
        prepareAndPrintCurrentNode(feature, {parentNode: rootNode, className: 'feature', heading: '<h1>'});
        featureElementsNode = currentNode.find('.childrenElements');
        featureElementsNode.addClass('featureElements');
    }

    this.background = function(background) {
        prepareAndPrintCurrentNode(background, {parentNode: featureElementsNode, className: 'background', heading: '<h2>'});
        prepareScenarioElementsNode();
    }

    this.scenario = function(scenario) {
        prepareAndPrintCurrentNode(scenario, {parentNode: featureElementsNode, className: 'scenario', heading: '<h2>'});
        prepareScenarioElementsNode();
    }

    this.scenarioOutline = function(outline) {
        this.scenario(outline);
        currentNode.addClass('outline');
    }

    this.step = function(step) {
        prepareAndPrintCurrentNode(step, {parentNode: scenarioElementsNode, className: 'step', heading: '<h3>'});
        currentNode.attr('id', step.id);
        if (hasExamples(step)) {
            printExamples(step.multiline_arg.value, false);
        }
    }
    
    this.examples = function(examples) {
        prepareAndPrintCurrentNode(examples, {parentNode: featureElementsNode, className: 'exampleBlock', heading: '<h2>'});
        printExamples(examples.rows, true);
    }
    
    var prepareScenarioElementsNode = function() {
        scenarioElementsNode = currentNode.find('.childrenElements');
        scenarioElementsNode.addClass('steps');
    }
    
    var prepareAndPrintCurrentNode = function(statement, nodeInfo) {
        currentNode = $('#templates .blockelement').clone().appendTo(nodeInfo.parentNode);
        currentNode.addClass(nodeInfo.className);
        printStatement(statement, nodeInfo.heading);
    }
    
    var hasExamples = function(step) {
        return step.multiline_arg !== undefined && step.multiline_arg.type === 'table';
    }
    
    var printExamples = function(examples, hasHeader) {
        var table = $('#templates .examples').clone().appendTo(currentNode.find('.childrenElements'));
        $.each(examples, function(index, example) {
            if (index === 0 && hasHeader) {
                node = table.find('thead');
            } else {
                node = table.find('tbody');
            }
            printExampleRow(node, example);
        });
    }
    
    var printExampleRow = function(node, example) {
        var tr = $('<tr>').appendTo(node);
        tr.attr('id', Cucumber.encodeId(currentUri, example.line));
        tr.addClass('exampleRow');
        $.each(example.cells,function(index, cell) {
            var td = $('<td>').appendTo(tr);
            td.addClass('exampleCell');
            td.text(cell);
        });
    }

    var printStatement = function(statement, heading) {
        currentNode.attr('id', Cucumber.encodeId(currentUri, statement.line));
        currentNode.find('.keyword').text(statement.keyword);
        currentNode.find('.name').text(statement.name);
        if (statement.description !== undefined && $.trim(statement.description) !== '') {
            currentNode.find('.description').text(statement.description);
        } else {
            currentNode.find('.description').remove();
        }
        currentNode.find('header').wrapInner(heading);
    }
}

Cucumber.Reporter = function() {
    var idMatched; 
    this.result = function(result) {
        $('#'+idMatched).addClass(result.status);
        if (result.error_message !== undefined) {
            $('<pre>').appendTo($('#'+idMatched)).text(result.error_message);
        }
    }
    this.match = function(match) {
        idMatched = Cucumber.encodeId(match.uri, match.step.line);
    }
}
