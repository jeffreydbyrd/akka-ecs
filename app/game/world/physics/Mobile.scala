package game.world.physics

import org.jbox2d.dynamics.Body

class Mobile( val body: Body ) {
  var floorsTouched = 0
  body.getFixtureList().setUserData( this )
}