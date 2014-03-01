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

/**************************************************************************
 * Mutable Objects
 **************************************************************************/

// create an new instance of a pixi stage
var stage = new PIXI.Stage(0x66FF99);

// create a renderer instance.
var renderer = PIXI.autoDetectRenderer(DIMENSIONS.w, DIMENSIONS.h);

// add the renderer view element to the DOM
document.body.appendChild(renderer.view);

// map unique IDs to sprites (eg. {'biff' : ..., 'wall' : ...})
var entities = {}

/**************************************************************************
 * Functions
 **************************************************************************/

/* 
 * On the server, (X,Y) positions refer to the center of objects.
 * Given a X position on a 0 to <INTERNAL_DIM> scale, and the width
 * of the object on the screen, we can derive where its left-most
 * position should be on the screen.
 */
function convertXPos(x) {
  return x * K;
}

/* Same as above but with Y position and height */
function convertYPos(y){
  return DIMENSIONS.h - (y * K);
}

/** data : { type : 'create', id : '...', position : [X, Y], dimensions : [W, H]  } */
function create(data) {
  requestAnimFrame(function(){
    var texture = PIXI.Texture.fromImage("/assets/images/Meebo2.png");
    var sprite = new PIXI.Sprite(texture);
    entities[data.id] = sprite;

    sprite.anchor.x = 0.5;
    sprite.anchor.y = 0.5;
    sprite.position.x = convertXPos(data.position[0]);
    sprite.position.y = convertYPos(data.position[1]);
    sprite.width = data.dimensions[0] * K;
    sprite.height = data.dimensions[1] * K;

    stage.addChild(sprite);
    renderer.render(stage);
  });
}

/** data : { type : 'quit', message : '...' } */
function quit(data) {
  conn.close();
  document.write("<p>" + data.message + "</p>");
}

function started(data) {
  conn.send({ type:"started" });
}

/* data: { type: 'move', id: '...', position:[X,Y] } */
function move(data) { 
  requestAnimFrame(function(){
    entities[data.id].position.x = convertXPos(data.position[0]);
    entities[data.id].position.y = convertYPos(data.position[1]);
    renderer.render(stage);
  });
}

function destroy(data) {

}

/**************************************************************************
 * Interface
 **************************************************************************/

/**
 * The set of functions that the server can execute remotely. Each function
 * takes a `data` object, which can take whatever form the function wants.
 */
var COMMANDS = {
  "started": started,
  "quit" : quit,
  "create" : create,
  "move" : move,
  "delete" : destroy
};
