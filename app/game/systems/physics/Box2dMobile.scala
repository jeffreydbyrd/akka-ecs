package game.systems.physics

import game.components.physics.Position
import org.jbox2d.dynamics.Body
import game.entity.Entity
import scala.math.abs
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Fixture

class Box2dMobile(
    var speed: Float,
    var hops: Float,
    var gravity: Float,
    val body: Body,
    val feet: Fixture,
    var grounded: Boolean ) {

  def setSpeed( speed: Float ) = {
    val vel = body.getLinearVelocity()
    var force: Float = 0

    if ( speed == 0 ) force = vel.x * -15
    else if ( ( abs( vel.x ) < abs( speed.toDouble ) )
      || ( vel.x >= 0 && speed < 0 )
      || ( vel.x <= 0 && speed > 0 ) ) force = speed * 20

    body.applyForce( new Vec2( force, 0 ), body.getWorldCenter() )
  }

  def jump() = {
    body.applyLinearImpulse( new Vec2( 0, -hops * gravity ), body.getWorldCenter() )
  }

}