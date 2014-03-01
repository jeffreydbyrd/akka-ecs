/***************************************************************************
 * Constants
 **************************************************************************/

var SCREEN_W = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
var SCREEN_H = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

var DIMENSIONS = {
  w : SCREEN_H / 1.1,
  h : SCREEN_H / 1.1
};

var INTERNAL_DIMENSIONS = 50;

/**
 * The server uses a room with <INTERNAL_DIMENSIONS> cells, so we need a multiplier. If the
 * server says "move 2 units left", we need to move (2*(DIMENSIONS.h / INTERNAL_DIMENSIONS)) 
 * pixels left.
 */
var K = DIMENSIONS.h / INTERNAL_DIMENSIONS;

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

/***************************************************************************
 * Prototypes
 **************************************************************************/

/**
 * A Connection sends messages to and receives messages from a server.
 * Connections expects JSON formatted strings. Connections also send ACKs
 * back to the server to confirm that a message was received.
 * 
 * @param url -
 *            a websocket URL string
 */
function Connection(url) {
  var self = this;

  var websocket = new WebSocket(url);

  websocket.onopen = function(evt) {
      console.log("websocket opened");
  };

  websocket.onclose = function() {
      console.log("websocket closed");
  };

  var expectedId = 0;

  /** 
   * evt.data : 
   *  { id : ???, 
   *    ack: true/false, 
   *    message: { type: ???, .... } } 
   */
  websocket.onmessage = function(evt) {
      var data = JSON.parse(evt.data);
      var msg = data.message;
      console.log(data);

      if (data.id <= expectedId) {
        self.ack(data.id);
      }

      if (data.id == expectedId) {
        COMMANDS[msg.type](msg);
        expectedId += 1;
      }
  };

  this.ack = function(id) {
    console.log("Sending ACK " + id);
    var ack = JSON.stringify({
      type : "ack",
      data : id
    });
    self.send(ack);     
  }

  this.send = function(data) {
    var str;
    if (typeof data === "string") {
      str = data;
    } else {
      str = JSON.stringify(data);
    }

    websocket.send(str);
  };

  this.close = function() {
      websocket.close();
  };

}

/**************************************************************************
* Objects
***************************************************************************/

var conn = new Connection(ADDRESS);

/***************************************************************************
 * Capture keydown & keyup events and send to the server
 **************************************************************************/

var onkey = function(evt) {
  var cmd = {
    type : evt.type,
    data : evt.keyCode
  };
  return conn.send(JSON.stringify(cmd));
};

var body = document.getElementById("body");
body.onkeydown = onkey;
body.onkeyup = onkey;

/***************************************************************************
 * Send a "quit" signal to the server just before the page unloads
 **************************************************************************/
window.onbeforeunload = function() {
  onkey({
    type : "keydown",
    keyCode : 81
  });
};
