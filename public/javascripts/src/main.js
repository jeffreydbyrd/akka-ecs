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

var game = new Game(LENGTH, LENGTH, K);
var listener = new InputListener(COMMANDS.keyBindings);
var conn = new Connection(ADDRESS);

game.bindTo(conn);
listener.bindTo(conn);

conn.start();
