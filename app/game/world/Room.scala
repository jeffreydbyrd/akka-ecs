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
import game.mobile.Movement
import game.mobile.Player
import game.util.math.Point
import org.jbox2d.dynamics.World
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.BodyType

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
  
  // define ground
  val groundBodyDef = new BodyDef()
  groundBodyDef.position.set( 0, 100 )
  groundBodyDef.`type` = BodyType.STATIC

  val groundBody = boxWorld.createBody( groundBodyDef )
  
  val groundBox = new PolygonShape()
  
  groundBox.setAsBox( 50.0f, 10.0f )
  val groundFixture = groundBody.createFixture( groundBox, 0.0f )

  val roomBehavior: Receive = {
    // create a new player, tell him to Start
    case Game.NewPlayer( client, name ) ⇒
      val plr = context.actorOf( Player.props( name ), name = name )
      subscribers += plr
      plr ! Player.Start( self, client )
    case Arrived   ⇒ sender ! RoomData( context.children )
    case mv: Moved ⇒ emit( mv )
    case Game.Tick ⇒ emit( Game.Tick )
  }

  override def receive = LoggingReceive {
    eventHandler orElse roomBehavior
  }

}