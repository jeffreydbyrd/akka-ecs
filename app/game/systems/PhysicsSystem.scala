package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.actorRef2Scala
import akka.pattern.{ ask, pipe }
import akka.actor.Actor
import akka.actor.Actor
import org.jbox2d.dynamics.World
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType
import game.core.Game
import game.entity.Entity
import game.components.ComponentType
import akka.actor.ActorRef
import game.components.Component.RequestSnapshot
import game.components.physics.PhysicalComponent
import akka.actor.Props

object PhysicsSystem {
  def props = Props( classOf[ PhysicsSystem ] )

  // Received
  case class CreateStructure( x: Float, y: Float, w: Float, h: Float )
  case class CreateMobile( x: Float, y: Float, w: Float, h: Float )
}

class PhysicsSystem( gx: Int, gy: Int ) extends System {
  import game.components.physics.PhysicalComponent._
  import PhysicsSystem._
  import System.UpdateComponents
  import Game.timeout

  val requiredComponents = List(
    ComponentType.Physical // Reads from, writes to
  // ComponentType.Velocity // Reads from, writes to
  )

  // Not really sure what these are for... all the tutorials use these values
  val timestep = 1.0f / 60.0f
  val velocityIterations = 6
  val positionIterations = 2

  // create a box2d world
  val world = new World( new Vec2( gx, gy ) )
  world.setAllowSleep( true )
  //  world.setContactListener( new MobileContactListener )

  protected case object Step // for personal use
  protected case class Node( val physicalComponent: ActorRef )

  var nodes: Map[ Node, Body ] = Map()

  def createStructure( x: Float, y: Float, w: Float, h: Float ): Body = {
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
    fixtureDef.density = 0

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

  override def receive = {
    case UpdateComponents( ents: Set[ Entity ] ) ⇒
      val newNodes =
        for ( e ← ents if e.hasComponents( requiredComponents ) )
          yield Node( e.components( ComponentType.Physical ) )

      for ( n ← newNodes -- nodes.keySet ) { // create new nodes
        ( n.physicalComponent ? RequestSnapshot ) foreach {
          case PhysicalComponent.Snapshot( p, rect ) ⇒
            self ! CreateStructure( p.x, p.y, rect.w, rect.h )
        }
      }

      for ( n ← nodes.keySet -- newNodes ) { // destroy old nodes
      }

    case CreateStructure( x, y, w, h ) ⇒ createStructure( x, y, w, h )

    case Step                          ⇒ world.step( timestep, velocityIterations, positionIterations )
    case Game.Tick                     ⇒
  }
}