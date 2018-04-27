var CucumberHTML = {};

Array.prototype.pushArray = function(arr) {
    this.push.apply(this, arr);
};

CucumberHTML.domTimelineContainer = null;
CucumberHTML.timelineGroups = [];
CucumberHTML.timelineItems = [];
CucumberHTML.timeline = null;

CucumberHTML.PreparePage = function () {
    CucumberHTML.RenderTimeline();
    CucumberHTML.bindScenarioSelector();
};

CucumberHTML.RenderTimeline = function () {

    CucumberHTML.domTimelineContainer = document.getElementById('timeline');

    var items = new vis.DataSet(CucumberHTML.timelineItems);

    var startTime = new Date(items.min("start").start);
    var endTime = new Date(items.max("end").end);

    // Configuration for the Timeline
    var options = {
        stack: false,
        min: startTime,
        max: endTime,
        dataAttributes: "all"
    };

    // Create a Timeline
    CucumberHTML.timeline = new vis.Timeline(CucumberHTML.domTimelineContainer, items, CucumberHTML.timelineGroups, options);

    CucumberHTML.timeline.on('click', function (props) {
        if (props.item !== null) {
            var div = $(document).find("[data-id='" + props.item + "']");
            alert(JSON.stringify(div.data('steps')));
        }
        props.event.preventDefault();
    });
};

CucumberHTML.bindScenarioSelector = function () {
    var sortedScenarios = CucumberHTML.timelineItems.sort(function(a,b) {
        if (a > b)
            return 1;
        return a < b ? -1 : 0;
    });

    var selector = $('#scenarioSelect');

    sortedScenarios.forEach(function(e) {
        selector.append($("<option></option>")
            .attr("value", e.id)
            .text(e.feature + " " + e.content));
    });

    selector.chosen();

    var selectOptions = {
        focus: true
    }

    selector.on('change', function() {
        CucumberHTML.timeline.setSelection(this.value, selectOptions);
    });
};
