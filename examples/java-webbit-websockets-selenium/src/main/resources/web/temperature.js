window.onload = function () {
    var ws = new WebSocket('ws://' + document.location.host + '/temperature');
    ws.onmessage = function (e) {
        var temp = e.data.split(':');
        document.getElementById(temp[0]).setAttribute('value', temp[1]);
    };

    function setupEvent(unit) {
        var c = document.getElementById(unit);
        c.onkeypress = function (e) {
            setTimeout(function () {
                ws.send(unit + ':' + e.target.value);
            }, 0);
        };
    }
    setupEvent('celcius');
    setupEvent('fahrenheit');
};