/*******************************************************************************
 * The DoppelClient
 * 
 * Depends on kinetic.js
 ******************************************************************************/
(function() {

	/***************************************************************************
	 * Constants
	 **************************************************************************/

	/** We will display a 600 x 600 px canvas */
	var DIMENSIONS = {
		w : 600,
		h : 600
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

		},
		"create" : function(data) {

		},
		"move" : function(data) {
			console.log(data);
		},
		"delete" : function(data) {

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
	 *            a URL string
	 */
	function Connection(url) {
		var self = this;

		this.websocket = new WebSocket(url);
		this.websocket.onopen = function(evt) {
			console.log("websocket opened");
		};

		this.websocket.onmessage = function(evt) {
			var cmd = JSON.parse(evt.data);
			COMMANDS[cmd.type](cmd);
		};

		this.send = function(str) {
			self.websocket.send(str);
		};

	}

	/**
	 * A View represents a set of canvases driven by Kinetic.js. A View has a
	 * set of `entities` that can move around the View
	 */
	function View() {
		this.stage = new Kinetic.Stage({
			container : 'container',
			width : DIMENSIONS.w,
			height : DIMENSIONS.h
		});
		var layer = new Kinetic.Layer();
		this.stage.add(layer);

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
				x : ent.position[0],
				y : ent.position[1],
				width : ent.dimensions[0],
				height : ent.dimensions[1],
				fill : 'black',
				stroke : 'black',
				strokeWidth : 1
			});
			layer.add(entities[id]);
		};

		/** Removes an entity from this view */
		this.remove = function(id) {
			entity[id].destroy();
		};

		/** Moves the `entity` with `id` to position (`x`, `y`) */
		this.move = function(id, x, y) {
			if (entity[id]) {
				entity[id].setX(x);
				entity[id].setY(DIMENSIONS.h - y);
			}
		};
	}

	/***************************************************************************
	 * Objects
	 **************************************************************************/

	var view = new View();

	var server = new Connection(ADDRESS);

	/***************************************************************************
	 * Capture keydown & keyup events and send to the server
	 **************************************************************************/

	var onkey = function(evt) {
		var cmd = {
			type : evt.type,
			data : evt.keyCode
		};
		return server.send(JSON.stringify(cmd));
	};

	var body = document.getElementById("body");
	body.onkeydown = onkey;
	body.onkeyup = onkey;

}).call(this);