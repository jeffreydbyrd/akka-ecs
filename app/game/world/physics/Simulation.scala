package game.world

import akka.actor.Actor
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import akka.actor.Props
import akka.actor.ActorRef
import akka.event.LoggingReceive
import game.mobile.Player
import akka.actor.actorRef2Scala

object Simulation {
  def props( gx: Int = 0, gy: Int = -9 ) = Props( classOf[ Simulation ], gx, gy )

  // Received Messages
  case object Step
  case class CreateBlock( x: Float, y: Float, w: Float, h: Float )
  case class CreateMobile( mobile: ActorRef, x: Float, y: Float, w: Float, h: Float )
  case class Checkup( mobile: ActorRef )

  // Sent Messages
  case class Snapshot( mobile: ActorRef, x: Float, y: Float )
}

/**
 * A 2D physics simulation, generally used to represent a World's or Room's physics.
 * Gravity represents real world gravity value (m/s^2). So gx defaults to 0, and
 * gy defaults to -9.
 */
class Simulation( gx: Int, gy: Int ) extends Actor {
  import Simulation._

  val timestep = 1.0f / 60.0f
  val velocityIterations = 6;
  val positionIterations = 2;

  // create a box2d world
  val world = new World( new Vec2( gx, gy ) )
  world.setAllowSleep( true )

  def createBlock( x: Float, y: Float, w: Float, h: Float ): Body = {
    val bodyDef = new BodyDef
    bodyDef.position.set( x, y )
    bodyDef.`type` = BodyType.STATIC

    val blockShape = new PolygonShape
    blockShape.setAsBox( w / 2, h / 2 )

    val fixtureDef = new FixtureDef
    fixtureDef.shape = blockShape

    val body: Body = world.createBody( bodyDef )
    body.createFixture( fixtureDef )
    body
  }

  def createMobile( x: Float, y: Float, w: Float, h: Float ): Body = {
    val mobileBodyDef = new BodyDef
    mobileBodyDef.fixedRotation = true
    mobileBodyDef.position.set( x, y )
    mobileBodyDef.`type` = BodyType.DYNAMIC

    val blockShape = new PolygonShape
    blockShape.setAsBox( w / 2, h / 2 )

    val fixtureDef = new FixtureDef
    fixtureDef.shape = blockShape
    fixtureDef.friction = 0

    val body: Body = world.createBody( mobileBodyDef )
    body.createFixture( fixtureDef )
    body
  }

  def setSpeed( body: Body, speed: Int ) = {
    val vel = body.getLinearVelocity()
    var force: Float = 0

    if ( speed == 0 ) force = vel.x * -15
    else if ( ( scala.math.abs( vel.x ) < scala.math.abs( speed.toDouble ) )
      || ( vel.x >= 0 && speed < 0 )
      || ( vel.x <= 0 && speed > 0 ) ) force = speed * 10

    body.applyForce( new Vec2( force, 0 ), body.getWorldCenter() )
  }

  var mobiles: Map[ ActorRef, Body ] = Map()

  override def receive = LoggingReceive {
    case Player.Quit( mob ) ⇒
      world.destroyBody( mobiles( mob ) )
      mobiles -= mob

    case CreateBlock( x, y, w, h ) ⇒ createBlock( x, y, w, h )

    case CreateMobile( mob, x, y, w, h ) ⇒
      val body = createMobile( x, y, w, h )
      mobiles += mob -> body

    case Step ⇒
      world.step( timestep, velocityIterations, positionIterations )
      for ( ( mob, body ) ← mobiles ) {
        val position = body.getPosition()
        context.parent ! Snapshot( mob, position.x, position.y )
      }

    case Player.Walking( mob, speed ) if mobiles.contains( mob ) ⇒ setSpeed( mobiles( mob ), speed )
    case Player.Standing( mob ) if mobiles.contains( mob )       ⇒ setSpeed( mobiles( mob ), 0 )
  }
}