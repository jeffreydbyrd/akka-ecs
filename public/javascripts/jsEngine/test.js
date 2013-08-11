$(document).ready( function() {

    var xpos = 0;

    var wsUrl = "ws://127.0.0.1:9000/test?username=jb";

    var ws = new WebSocket(wsUrl);

    ws.onopen = function(evt) {
	console.log("websocket opened");
    };

    ws.onclose = function(evt) {
	console.log("websocket closed");
    };

    ws.onmessage = function(evt) {
	console.log(evt.data);
	$('#xpos').html(evt.data);
    };


    var keydown = false;

    $('html').keydown(function(evt) {
	if (!keydown) {
	    keydown = true;
	    var cmd = {
		type: 'keydown',
		data: evt.which
	    };
	    ws.send(JSON.stringify(cmd));
	}
    });


    $('html').keyup(function(evt) {
	keydown = false;
	var cmd = {
	    type:'keyup',
	    data: evt.which
	};
	ws.send(JSON.stringify(cmd));
    });


});
