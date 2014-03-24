function Commands() {
  var jump = "JUMP";
  var left = "LEFT";
  var right = "RIGHT";
  var quit = "QUIT";

  this.keyBindings = {
    32:jump, 38:jump, 87:jump,
    65:left, 37:left,
    68:right, 39:right,
    81:quit
  };
}

var COMMANDS = new Commands();