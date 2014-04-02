package game.world.physics

import scala.concurrent.ExecutionContext.Implicits.global
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.mobile.ClientProxy
import game.util.logging.AkkaLoggingService
import game.Game
import scala.concurrent.duration._

object Simulation {
  def props( gx: Int, gy: Int ) = Props( classOf[ Simulation ], gx, gy )

  // Received Messages
  case object Step
  case object EmitSnapshot
  case class CreateBlock( x: Float, y: Float, w: Float, h: Float )
  case class CreateMobile( mobile: ActorRef, x: Float, y: Float, w: Float, h: Float )
  case class Checkup( mobile: ActorRef )

  // Sent Messages
  case class Snapshot( positions: Map[ ActorRef, ( Float, Float ) ] )
}

/**
 * An actor that simulates a 2D Room's physics
 */
class Simulation( gx: Int, gy: Int ) extends Actor {
  import Simulation._

  val logger = new AkkaLoggingService( this, context )
  val snapshotTimer =
    Game.system.scheduler.schedule( 100 milliseconds, 50 milliseconds, self, EmitSnapshot )

  val jumpImpulse = -0.5 * gy
  val mobileMass = 2

  // Not really sure what these are for... all the tutorials use these values
  val timestep = 1.0f / 60.0f
  val velocityIterations = 6
  val positionIterations = 2

  // create a box2d world
  val world = new World( new Vec2( gx, gy ) )
  world.setAllowSleep( true )
  world.setContactListener( new MobileContactListener )

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
    fixtureDef.density = mobileMass

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
      || ( vel.x <= 0 && speed > 0 ) ) force = speed * 20

    body.applyForce( new Vec2( force, 0 ), body.getWorldCenter() )
  }

  def jump( body: Body, force: Double ) = {
    val impulse = body.getMass() * force
    body.applyLinearImpulse( new Vec2( 0, impulse.toFloat ), body.getWorldCenter() )
  }

  var mobiles: Map[ ActorRef, Mobile ] = Map()

  override def receive = LoggingReceive {
    case ClientProxy.Quit( mob ) if mobiles.contains( mob ) ⇒
      world.destroyBody( mobiles( mob ).body )
      mobiles -= mob

    case CreateBlock( x, y, w, h ) ⇒ createBlock( x, y, w, h )

    case CreateMobile( mob, x, y, w, h ) ⇒
      val body = createMobile( x, y, w, h )
      mobiles += mob -> new Mobile( body )

    case Step ⇒ world.step( timestep, velocityIterations, positionIterations )

    case EmitSnapshot ⇒
      var positions: Map[ ActorRef, ( Float, Float ) ] = Map()
      for ( ( ref, mob ) ← mobiles ) {
        val pos = mob.body.getPosition()
        positions += ref -> ( pos.x, pos.y )
      }
      context.parent ! Snapshot( positions )

//    case ClientProxy.WalkAttempt( mob, speed ) if mobiles.contains( mob ) ⇒
//      setSpeed( mobiles( mob ).body, speed )

//    case ClientProxy.JumpAttempt( mob ) if mobiles.contains( mob ) && mobiles( mob ).floorsTouched > 0 ⇒
//      jump( mobiles( mob ).body, jumpImpulse )
  }
}