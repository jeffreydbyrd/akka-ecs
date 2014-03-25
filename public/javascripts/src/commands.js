function Commands() {
  this.jump = "JUMP";
  this.left = "LEFT";
  this.right = "RIGHT";
  this.quit = "QUIT";

  this.keyBindings = {
    32:this.jump, 38:this.jump, 87:this.jump,
    65:this.left, 37:this.left,
    68:this.right, 39:this.right,
    81:this.quit
  };
}

var COMMANDS = new Commands();