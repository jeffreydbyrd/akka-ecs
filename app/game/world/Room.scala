package game.world

import scala.math.BigDecimal.int2bigDecimal
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.events.Adjust
import game.events.Event
import game.events.EventHandler
import game.mobile.Mobile.Moved
import game.mobile.Player
import game.util.math.Point
import org.jbox2d.dynamics.World
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.AABB
import org.jbox2d.dynamics.Body

object Room {
  def props( name: String ) = Props( classOf[ Room ], name )

  // Received Messages
  case object Arrived extends Event

  // Sent Messages
  case class RoomData( children: Iterable[ ActorRef ] ) extends Event

  val gravity: Int = -10
}

/**
 * An ActorEventHandler that mediates almost all Events that propagate through the world.
 * Every Room in existence shares the same 4 Surfaces to form a box that contains mobiles.
 */
class Room( val id: String ) extends EventHandler {
  import Room._

  val boxWorld = new World( new Vec2( 0, gravity ) )

  // define ground body
  val groundBodyDef = new BodyDef
  groundBodyDef.position.set( 0, 0 )
  groundBodyDef.`type` = BodyType.STATIC

  // define ground shape
  val groundShape = new PolygonShape
  groundShape.setAsBox( 200, 1 )

  // define ground fixture
  val groundFixtureDef = new FixtureDef
  groundFixtureDef.shape = groundShape
  groundFixtureDef.density = 1

  // add body and give it a fixture
  val body: Body = boxWorld.createBody( groundBodyDef )
  body.createFixture( groundFixtureDef )

  val timestep = 1.0f / 60.0f
  val velocityIterations = 6;
  val positionIterations = 2;

  // mobile body def
  val mobileBodyDef = new BodyDef
  mobileBodyDef.position.set( 10, 200 )
  mobileBodyDef.`type` = BodyType.DYNAMIC

  // mobile shape:
  val mobileShape = new PolygonShape
  mobileShape.setAsBox( 4, 2 )

  // mobile fixture def
  val mobileFixtureDef = new FixtureDef
  mobileFixtureDef.shape = mobileShape
  mobileFixtureDef.density = 1

  var mobiles: Map[ ActorRef, Body ] = Map()

  val roomBehavior: Receive = {
    // create a new player, tell him to Start
    case Game.NewPlayer( client, name ) ⇒
      val plr = context.actorOf( Player.props( name ), name = name )
      subscribers += plr
      plr ! Player.Start( self, client )
    case Arrived ⇒
      val body = boxWorld.createBody( mobileBodyDef )
      body.createFixture( mobileFixtureDef )
      mobiles += sender -> body
      sender ! RoomData( subscribers )
    case mv: Moved ⇒ emit( mv )
    case Game.Tick ⇒
      boxWorld.step( timestep, velocityIterations, positionIterations )
      for {
        ( ref, body ) ← mobiles
      } {
        val position = body.getPosition()
        ref ! Moved( ref, position.x, position.y )
      }
      emit( Game.Tick )
  }

  override def receive = LoggingReceive {
    eventHandler orElse roomBehavior
  }

}