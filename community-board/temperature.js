// Buzzer => https://software.intel.com/en-us/node/557494


// Load Grove module
var groveSensor = require('jsupm_grove');

// Create the temperature sensor object using AIO pin 0
var temp = new groveSensor.GroveTemp(0);
console.log(temp.name());


var alertQty = 0;
var minimumDurationBeforeAlerting = 10;

// Read the temperature ten times, printing both the Celsius and
// equivalent Fahrenheit temperature, waiting one second between readings
var i = 0;
var waiting = setInterval(function () {
    var celsius = temp.value();
    var fahrenheit = celsius * 9.0 / 5.0 + 32.0;
    console.log(celsius + " degrees Celsius, or " +
        Math.round(fahrenheit) + " degrees Fahrenheit");
    i++;

    if (temp.value() > 30) {
        alertQty++;
    }
    else {
        alertQty--;
        alertQty = Math.max(0, alertQty);
    }
    if (alertQty > minimumDurationBeforeAlerting) {
        console.log("alertQty", alertQty);
        sendAlert();
        alertQty = -10;
    }

}, 1000);

request = require('request-json');
var client = request.createClient('http://community.vdurnez.com');

var sendAlert = function () {
    var data = {
        location: "10 rainmaking loft, london",
        sensor: "fire",
        value: 50
    };

    client.post('/1/sensor/alert/12345', data, function (err, res, body) {
        return console.log(res.statusCode);
    });
};
