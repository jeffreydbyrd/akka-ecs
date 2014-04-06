function Game(canvasWidth, canvasHeight, k) {
  var self = this;

  self.stage = new PIXI.Stage(0x66FF99);
  self.renderer = PIXI.autoDetectRenderer(canvasWidth, canvasHeight);
  document.body.appendChild(self.renderer.view);

  // map unique IDs to sprites (eg. {'biff' : ..., 'wall' : ...})
  self.entities = {};

  self.rendering = false;
  self.renderStage = function() {
    self.rendering = false;
    self.renderer.render(self.stage);
  }

  self.attemptRender = function() {
    console.log(self.rendering)
    if (!self.rendering) {
      requestAnimFrame(self.renderStage);
      self.rendering = true;
    }
  }

  function convertXPos(x) { return x * k }
  function convertYPos(y){ return canvasHeight - (y * k) }

  self.create = function(id, x, y, w, h) {
    var texture = PIXI.Texture.fromImage("/assets/images/black.png");
    var sprite = new PIXI.Sprite(texture);
    self.entities[id] = sprite;

    sprite.anchor.x = 0.5;
    sprite.anchor.y = 0.5;
    sprite.position.x = convertXPos(x);
    sprite.position.y = convertYPos(y);
    sprite.width = w * k;
    sprite.height = h * k;

    self.stage.addChild(sprite);
    self.attemptRender();
  };

  self.move = function(id, x, y) {
    self.entities[id].position.x = convertXPos(x);
    self.entities[id].position.y = convertYPos(y);
    self.attemptRender();
  };

  self.bindTo = function(conn) {
    conn.onReceive("started", function(args) {
      conn.send({ type:"started" })
    });

    conn.onReceive("create", function(params) {
      console.log(self);
      self.create( params.id, params.position[0], params.position[1],
                   params.dimensions[0], params.dimensions[1] );
      self.attemptRender();
    });

    conn.onReceive("update_positions", function(params) {
      for(id in params) {
        self.move(id, params[id][0], params[id][1]);
      }
    });

    conn.onReceive("quit", function(params) {
      conn.close();
      document.write("<p>later!</p>");
    });

  };
}
