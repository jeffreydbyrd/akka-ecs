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
var length = UTIL.screen_h / 1.1;
var k = length / internal_dimensions;

var game = new Game(length, length, k);
var listener = new InputListener(COMMANDS.keyBindings);
var conn = new Connection(address);

game.bindTo(conn);
listener.bindTo(conn);

conn.start();

requestAnimFrame( animate );
function animate() {
  requestAnimFrame( animate );
  game.renderStage();
}
