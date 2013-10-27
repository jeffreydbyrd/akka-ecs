/*******************************************************************************
 * The DoppelClient
 * 
 * Depends on kinetic.js
 ******************************************************************************/
(function() {

	/***************************************************************************
	 * Constants
	 **************************************************************************/
	var DIMENSIONS = {
		w : 200,
		h : 200
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
		this.websocket = new WebSocket(url);
	}
	
	/**
	 * The view encapsulates the set of Kinetic canvas objects.
	 */
	var view = {
		/** The stage is the root container of */
		stage : new Kinetic.Stage({
			container : 'container',
			width : DIMENSIONS.w,
			height : DIMENSIONS.h
		});

		mobiles : new Kinetic.Layer();
	};
	
}).call(this);