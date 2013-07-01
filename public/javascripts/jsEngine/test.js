$(document).ready( function() {

    var wsUrl = "ws://127.0.0.1:9000/test";

    var ws = new WebSocket(wsUrl);

    ws.onopen = function(evt) {
	console.log("websocket opened");
    };

    ws.onclose = function(evt) {
	console.log("websocket closed");
    };

    ws.onmessage = function(evt) {
	console.log(evt.data);
    };

    $("#send").click(function(){
	ws.send("hello world");
    });

    $("#close").click(function(){
	ws.close();
    });

});
