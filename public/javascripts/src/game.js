function Game(canvasWidth, canvasHeight, k) {
  var stage = new PIXI.Stage(0x66FF99);
  var renderer = PIXI.autoDetectRenderer(canvasWidth, canvasHeight);
  document.body.appendChild(renderer.view);

  // map unique IDs to sprites (eg. {'biff' : ..., 'wall' : ...})
  var entities = {};

  this.create = function(id, x, y, w, h) {
    // requestAnimFrame(function(){
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
      renderer.render(stage);
    // });
  }

  this.move = function(id, x, y) {
    // requestAnimFrame(function(){
      entities[id].position.x = convertXPos(x);
      entities[id].position.y = convertYPos(y);
      renderer.render(stage);
    // });
  }

  /* 
   * On the server, (X,Y) positions refer to the center of objects.
   * Given a X position on a 0 to <INTERNAL_DIM> scale, and the width
   * of the object on the screen, we can derive where its left-most
   * position should be on the screen.
   */
  function convertXPos(x) {
    return x * k;
  }
  
  /* Same as above but with Y position and height */
  function convertYPos(y){
    return canvasHeight - (y * k);
  }
}