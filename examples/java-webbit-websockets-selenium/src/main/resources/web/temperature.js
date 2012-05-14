window.onload = function () {
    var ws = new WebSocket('ws://' + document.location.host + '/temperature');
    ws.onmessage = function (e) {
        var temp = e.data.split(':');
        document.getElementById(temp[0]).setAttribute("value", temp[1]);
    };

    var c = document.getElementById('celcius');
    c.onkeypress = function (e) {
        setTimeout(function () {
            ws.send("celcius:" + e.target.value);
        }, 0);
    };
};