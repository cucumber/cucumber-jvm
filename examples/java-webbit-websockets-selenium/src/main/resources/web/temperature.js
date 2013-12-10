window.onload = function () {
  var ws = new WebSocket('ws://' + document.location.host + '/temperature');

  ws.onopen = function () {
    // Only enable the input fields after we have established a WebSocket connection.
    document.getElementById("celcius").disabled = false;
    document.getElementById("fahrenheit").disabled = false;
  };

  ws.onmessage = function (e) {
    var temp = e.data.split(':');
    document.getElementById(temp[0]).value = temp[1];
  };

  function setupEvent(unit) {
    var c = document.getElementById(unit);
    c.onkeyup = function (e) {
      setTimeout(function () {
        ws.send(unit + ':' + e.target.value);
      }, 0);
    };
  }

  setupEvent('celcius');
  setupEvent('fahrenheit');
};
