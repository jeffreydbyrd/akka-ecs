/**
 * A Connection sends messages to and receives messages from a server.
 * Connections expects JSON formatted strings. Connections also send ACKs
 * back to the server to confirm that a message was received.
 */
function Connection(url) {
  var self = this;
  var socket;
  var callbacks = {};

  this.isClosed = true;

  this.onReceive = function(type, f) { callbacks[type] = f };

  this.start = function() {
    socket = new WebSocket(url);
    this.isClosed = false;
    socket.onopen = function(evt) { console.log("websocket opened") };
    socket.onclose = function() { console.log("websocket closed") };
    socket.onmessage = function(evt) { receive(JSON.parse(evt.data)) };
  };

  this.close = function() { socket.close(); this.isClosed = true; };

  this.send = function(data) {
    if (this.isClosed) {
      return;
    }

    if (typeof data === "string")
      socket.send(data);
    else
      socket.send( JSON.stringify(data) );
  };

  var expectedSeq = 0;

  function receive(data) {
    console.log(data);
    var params = data.message;

    if (data.seq <= expectedSeq) {
      ack(data.seq);
    }

    if (data.seq == expectedSeq) {
      callbacks[data.type](params);
      if (data.ack) expectedSeq += 1;
    }
  }

  function ack(id) {
    var ack = {
      type : "ack",
      data : id
    };
    self.send(ack);
  }
}
