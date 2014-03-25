function contains(a, obj) {
  for (var i = 0; i < a.length; i++) {
    if (a[i] === obj) {
      return true;
    }
  }
  return false;
}

function InputListener(keybindings) {
	var self = this;
	var connection;

	// For some keys, we need to know when the user releases it:
  var needsReleased = [COMMANDS.left, COMMANDS.right];

  function keyDown(evt) {
    var type = keybindings[evt.keyCode];
    if (contains(needsReleased, type)) {
      type = "GO_" + type;
    }
    var msg = {'type': type};
    console.log(msg);
    self.connection.send(msg);
  }

  function keyUp(evt) {
    var type = keybindings[evt.keyCode];
    if (contains(needsReleased, type)) {
      self.connection.send({'type': "STOP_"+type});
    }
  }

  this.bindTo = function(connection) {
    self.connection = connection;
    document.body.onkeydown = keyDown;
    document.body.onkeyup = keyUp;
  }
}