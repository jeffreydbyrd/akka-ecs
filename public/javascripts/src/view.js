function View(canvasX, canvasY, kx, ky) {
  var self = this;

  var stage = new PIXI.Stage(0x66FF99);
  var renderer = PIXI.autoDetectRenderer(canvasX, canvasY);
  document.body.appendChild(renderer.view);

  // map unique IDs to sprites (eg. {'biff' : ..., 'wall' : ...})
  var entities = {};

  // Game loop:
  requestAnimFrame( animate );
  function animate() {
    requestAnimFrame( animate );
    renderer.render(stage);
  }

  window.onresize = function(event) {
    console.log(event);
  }

  function convertXPos(x) { return x * kx }
  function convertYPos(y){ return canvasY - (y * ky) }

  self.create = function(id, x, y, w, h) {
    var texture = PIXI.Texture.fromImage("/assets/images/black.png");

    var sprite = new PIXI.Sprite(texture);
    sprite.anchor.x = 0.5;
    sprite.anchor.y = 0.5;
    sprite.position.x = convertXPos(x);
    sprite.position.y = convertYPos(y);
    sprite.width = w * kx;
    sprite.height = h * ky;
    stage.addChild(sprite);

    entities[id] = {
      'sprite':sprite,
      'x':x, 'y':y,
      'w':w, 'h':h
    };
  };

  self.move = function(id, x, y) {
    entities[id].x = x;
    entities[id].y = y;
    entities[id].sprite.position.x = convertXPos(x);
    entities[id].sprite.position.y = convertYPos(y);
  };

  self.bindTo = function(conn) {
    conn.onReceive("create", function(params) {
      self.create( params.id, params.position[0], params.position[1],
                   params.dimensions[0], params.dimensions[1] );
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
