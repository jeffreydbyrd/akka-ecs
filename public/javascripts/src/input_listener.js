function InputListener(keybindings) {
	var self = this;
	var connection;

	// For some keys, we need to know when the user releases it:
  var needsReleased = [COMMANDS.left, COMMANDS.right];

  function keyDown(evt) {
    var type = keybindings[evt.keyCode];
    if (UTIL.contains(needsReleased, type)) {
      type = "GO_" + type;
    }
    self.connection.send({'type': type});
  }

  function keyUp(evt) {
    var type = keybindings[evt.keyCode];
    if (UTIL.contains(needsReleased, type)) {
      self.connection.send({'type': "STOP_"+type});
    }
  }

  this.bindTo = function(connection) {
    self.connection = connection;
    document.body.onkeydown = keyDown;
    document.body.onkeyup = keyUp;
  }
}
