var CucumberHTML = {};

Array.prototype.pushArray = function (arr) {
    this.push.apply(this, arr);
};

CucumberHTML.domTimelineContainer = null;
CucumberHTML.timelineGroups = [];
CucumberHTML.timelineItems = [];
CucumberHTML.timeline = null;

CucumberHTML.PrepareData = function () {
    $.each(CucumberHTML.timelineItems, function (index, item) {
        item.content = item.feature + '<br/>' + item.scenario;
    });
};

CucumberHTML.PreparePage = function () {
    CucumberHTML.RenderTimeline(CucumberHTML.timelineItems);
    CucumberHTML.bindScenarioSelector(CucumberHTML.timelineItems);
    CucumberHTML.bindTestWithTagSelector();
};

CucumberHTML.RenderTimeline = function (timelineItems) {
    if (CucumberHTML.timeline !== null) {
        CucumberHTML.timeline.destroy();
    }

    CucumberHTML.domTimelineContainer = document.getElementById('timeline');

    var items = new vis.DataSet(timelineItems);

    var startTime = new Date(items.min("start").start);
    var endTime = new Date(items.max("end").end);

    // Configuration for the Timeline
    var options = {
        stack: false,
        min: startTime,
        max: endTime,
        groupOrder: function (a, b) {
            return a.id - b.id;
        }
    };

    // Create a Timeline
    CucumberHTML.timeline = new vis.Timeline(CucumberHTML.domTimelineContainer, items, CucumberHTML.timelineGroups, options);
};

CucumberHTML.bindScenarioSelector = function (timelineItems) {
    var sortedScenarios = timelineItems.sort(function (a, b) {
        if (a > b)
            return 1;
        return a < b ? -1 : 0;
    });

    var selector = $('#scenarioSelect');

    sortedScenarios.forEach(function (e) {
        selector.append($("<option></option>")
            .attr("value", e.id)
            .text(e.feature + " " + e.scenario));
    });

    selector.chosen();

    var selectOptions = {
        focus: true
    };

    selector.on('change', function () {
        CucumberHTML.timeline.setSelection(this.value, selectOptions);
    });
};

CucumberHTML.bindTestWithTagSelector = function () {
    var allTags = [];
    CucumberHTML.timelineItems.forEach(function (test) {
        if (test.tags !== null && test.tags !== "") {
            var tags = test.tags.split(",");
            for (var i = 0; i < tags.length; i++) {
                var tag = tags[i];
                if (tag !== null && tag !== "" && $.inArray(tag, allTags) === -1) {
                    allTags.push(tag);
                }
            }
        }
    });

    allTags.sort();
    var selector = $('#tagSelect');

    allTags.forEach(function (e) {
        selector.append($("<option></option>")
            .attr("value", e)
            .text(e));
    });

    selector.chosen();

    selector.on('change', function () {
        var selectedTag = this.value;
        var filteredTimelineItems = [];
        CucumberHTML.timelineItems.forEach(function (test) {
            var tags = test.tags.split(",");
            if ($.inArray(selectedTag, tags) !== -1) {
                filteredTimelineItems.push(test);
            }
        });

        if (filteredTimelineItems.length > 0) {
            CucumberHTML.RenderTimeline(filteredTimelineItems);
            CucumberHTML.bindScenarioSelector(filteredTimelineItems);
        }
    });
};

function resetTimeline() {
    CucumberHTML.PreparePage();
}
