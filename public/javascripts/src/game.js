function Game(canvasWidth, canvasHeight, k) {
  var self = this;

  var stage = new PIXI.Stage(0x66FF99);
  var renderer = PIXI.autoDetectRenderer(canvasWidth, canvasHeight);
  document.body.appendChild(renderer.view);

  // map unique IDs to sprites (eg. {'biff' : ..., 'wall' : ...})
  var entities = {};

  var rendering = false;
  function renderStage() {
    rendering = false;
    renderer.render(stage);
  }

  function attemptRender() {
    if (!rendering) {
      requestAnimFrame(renderStage);
      rendering = true;
    }
  }

  function convertXPos(x) { return x * k }
  function convertYPos(y){ return canvasHeight - (y * k) }

  this.create = function(id, x, y, w, h) {
    var texture = PIXI.Texture.fromImage("/assets/images/black.png");
    var sprite = new PIXI.Sprite(texture);
    entities[id] = sprite;

    sprite.anchor.x = 0.5;
    sprite.anchor.y = 0.5;
    sprite.position.x = convertXPos(x);
    sprite.position.y = convertYPos(y);
    sprite.width = w * k;
    sprite.height = h * k;

    stage.addChild(sprite);
    attemptRender();    
  };

  this.move = function(id, x, y) {
    entities[id].position.x = convertXPos(x);
    entities[id].position.y = convertYPos(y);
    attemptRender();
  };

  this.bindTo = function(conn) {
    conn.onReceive("started", function(args) { 
      conn.send({ type:"started" }) 
    });

    conn.onReceive("create", function(params) {
      self.create( params.id, params.position[0], params.position[1],
                  params.dimensions[0], params.dimensions[1] );
    });

    conn.onReceive("move", function(params) {
      self.move(params.id, params.position[0], params.position[1])
    });

    conn.onReceive("quit", function(params) {
      conn.close();
      document.write("<p>" + params.message + "</p>");
    });

  };
}
