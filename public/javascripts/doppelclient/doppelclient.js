/*******************************************************************************
 * The DoppelClient
 * 
 * Depends on kinetic.js
 ******************************************************************************/
(function() {

	/***************************************************************************
	 * Constants
	 **************************************************************************/

	/** We will display a 400 x 400 px canvas */
	var DIMENSIONS = {
		w : 400,
		h : 400
	};

	/**
	 * The server expects a 200 x 200 cell room, so we need a multiplier. If the
	 * server says "move 2 units left", we need to move 6 pixels left.
	 */
	var K = DIMENSIONS.h / 200;

	var ADDRESS = "ws://127.0.0.1:9000/test?username=jb";

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
			console.log(data);
		},
		"move" : function(data) {
			view.move(data.id, data.position[0], data.position[1]);
			console.log(data);
		},
		"delete" : function(data) {
			view.remove(data.id);
		}
	}

	/***************************************************************************
	 * Prototypes
	 **************************************************************************/

	/**
	 * A Connection sends messages to and receives messages from a server.
	 * Connections expects JSON formatted strings.
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
		}

		websocket.onmessage = function(evt) {
			var msg = JSON.parse(evt.data);
			var id = msg.id;
			var cmd = msg.message;
			var ack = JSON.stringify({
				type : "ack",
				data : id
			});
			websocket.send(ack);
			COMMANDS[cmd.type](cmd);
			view.draw();
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