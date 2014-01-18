/*******************************************************************************
 * The DoppelClient
 * 
 * Depends on kinetic.js. Extracts a `username` from the URL
 * (http://a.b.c.d:80#username=???) and establishes a WebSocket connection with
 * the server using `username`.
 ******************************************************************************/
(function() {

    /***************************************************************************
     * Constants
     **************************************************************************/

    var SCREEN_W = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
    var SCREEN_H = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

    var DIMENSIONS = {
	w : SCREEN_H / 1.1,
	h : SCREEN_H / 1.1
    };

    /**
     * The server expects a 200 x 200 cell room, so we need a multiplier. If the
     * server says "move 2 units left", we need to move (2*(DIMENSIONS.h / 200)) pixels left.
     */
    var K = DIMENSIONS.h / 200;

    var USERNAME = function() {
	var hash = window.location.hash;
	var i = hash.indexOf("username=") + 9
	return hash.substring(i);
    }();

    var ADDRESS = "ws://127.0.0.1:9000/test?username=" + USERNAME;

    /**
     * The set of functions that the server can execute remotely. Each function
     * takes a `data` object, which can take whatever form the function wants.
     */
    var COMMANDS = {
	"quit" : function(data) {
	    conn.close();
	    document.write("<p>" + data.message + "</p");
	},
	"create" : function(data) {
	    view.add(data);
	    view.draw();
	    console.log(data);
	},
	"move" : function(data) {
	    view.move(data.id, data.position[0], data.position[1]);
	    view.draw();
	    console.log(data);
	},
	"delete" : function(data) {
	    view.remove(data.id);
	}
    };

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
	var count = 0;

	websocket.onopen = function(evt) {
	    console.log("websocket opened");
	};

	websocket.onclose = function() {
	    console.log("websocket closed");
	};

	/** evt.data schema === {id : ???, message: { type: ???, .... } } */
	websocket.onmessage = function(evt) {
	    console.log(evt.data);
	    var msg = JSON.parse(evt.data);
	    var id = msg.id;
	    count = id;
	    var cmd = msg.message;
	    COMMANDS[cmd.type](cmd);

	    var ack = JSON.stringify({
		type : "ack",
		data : id
	    });
	    websocket.send(ack);
	};

	this.send = function(str) {
	    websocket.send(str);
	};

	this.close = function() {
	    websocket.close();
	};

    }

    /**
     * A View represents a set of canvases driven by Kinetic.js. A View has a
     * set of `entities` that can move around the View
     */
    function View() {
	var stage = new Kinetic.Stage({
	    container : 'container',
	    width : DIMENSIONS.w,
	    height : DIMENSIONS.h
	});
	document.getElementsByClassName("kineticjs-content")[0].style["border"] = "1px solid black";

	var layer = new Kinetic.Layer();
	stage.add(layer);

	/**
	 * `entities` is a mapping between `id`s and Kinetic Nodes that move
	 * around the screen
	 */
	var entities = {};

	/**
	 * Adds an entity to this view. The expected schema for `ent` is:
	 * {"id":"...", "dimensions":[W,H], "position":[X,Y]}
	 */
	this.add = function(ent) {
	    var id = ent.id;
	    if (!entities[id]) {
		entities[id] = new Kinetic.Rect({
		    x : ent.position[0] * K,
		    y : ent.position[1] * K,
		    width : ent.dimensions[0] * K,
		    height : ent.dimensions[1] * K,
		    fill : 'black',
		    stroke : 'black',
		    strokeWidth : 1
		});
		layer.add(entities[id]);
	    }
	};

	/** Removes an entity from this view */
	this.remove = function(id) {
	    if (entities[id])
		entities[id].destroy();
	};

	/** Moves the `entity` with `id` to position (`x`, `y`) */
	this.move = function(id, x, y) {
	    if (entities[id]) {
		entities[id].setX(x * K);
		entities[id].setY(DIMENSIONS.h - (y * K));
	    }
	};

	this.draw = function() {
	    stage.draw();
	};
    }

    /***************************************************************************
     * Objects
     **************************************************************************/

    var view = new View();

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

}).call(this);
