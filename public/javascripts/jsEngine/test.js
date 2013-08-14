$(document).ready( function() {

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

    function onKey(evt) {
	var cmd = { type: evt.type, data: evt.keyCode };
	ws.send(JSON.stringify(cmd));
    }    

    $('html').keydown( onKey );

    $('html').keyup( onKey );
    
    $(window).unload(function(evt) {
	evt.type = 'keyup';
	evt.keyCode = '81'; // Q for quit
	onKey(evt);
    });

});
