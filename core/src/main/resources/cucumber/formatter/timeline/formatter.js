var CucumberHTML = {};

Array.prototype.pushArray = function(arr) {
    this.push.apply(this, arr);
};

CucumberHTML.domTimelineContainer = null;
CucumberHTML.timelineGroups = [];
CucumberHTML.timelineItems = [];

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
    CucumberHTML.timelineItems = new vis.Timeline(CucumberHTML.domTimelineContainer, items, CucumberHTML.timelineGroups, options);
};
