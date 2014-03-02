/* Constants */

var USERNAME = function() {
  var hash = window.location.hash;
  var i = hash.indexOf("username=") + 9
  return hash.substring(i);
}();

var ADDRESS = function() {
  var href = window.location.href;
  var addr = href.substring(7, href.indexOf("/#"));
  return "ws://" + addr + "/test?username=" + USERNAME;
}();

var SCREEN_H = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
var LENGTH = SCREEN_H / 1.1;

var INTERNAL_DIMENSIONS = 50;
var K = LENGTH / INTERNAL_DIMENSIONS;

// Make a game object. It renders itself:
var game = new Game(LENGTH, LENGTH, K);

/* Make and configure our Connetion object */
var conn = new Connection(ADDRESS);

conn.onReceive("started", function(args) { 
  conn.send({ type:"started" }) 
});

conn.onReceive("create", function(params) {
  game.create(
    params.id, params.position[0], params.position[1],
    params.dimensions[0], params.dimensions[1]
  );
});

conn.onReceive("move", function(params) {
  game.move(params.id, params.position[0], params.position[1])
});

conn.onReceive("quit", function(params) {
  conn.close();
  document.write("<p>" + params.message + "</p>");
});

conn.start();

/* Capture keydown & keyup events and send to the server */
var onkey = function(evt) {
  var cmd = {
    type : evt.type,
    data : evt.keyCode
  };
  return conn.send(cmd);
};

var body = document.getElementById("body");
body.onkeydown = onkey;
body.onkeyup = onkey;

/* Send a "quit" signal to the server just before the page unloads */
window.onbeforeunload = function() {
 	var cmd = {
 		type : "keydown",
 		data : 81
 	};
 	return conn.send(cmd);
};
