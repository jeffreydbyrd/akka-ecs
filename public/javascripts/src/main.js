var username = function() {
  var hash = window.location.hash;
  var i = hash.indexOf("username=") + 9
  return hash.substring(i);
}();

var address = function() {
  var href = window.location.href;
  var addr = href.substring(7, href.indexOf("/#"));
  return "ws://" + addr + "/test?username=" + username;
}();

var internal_dimensions = 50;
var canvasy = UTIL.screenh() / 1.03;
var canvasx = UTIL.screenw() / 1.03;
var ky = canvasy / internal_dimensions;
var kx = canvasx / internal_dimensions;

var view = new View(canvasx, canvasy, kx, ky);
var listener = new InputListener(COMMANDS.keyBindings);
var conn = new Connection(address);

view.bindTo(conn);
listener.bindTo(conn);
conn.start();
