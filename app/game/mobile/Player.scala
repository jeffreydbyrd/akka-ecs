package game.mobile

import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.communications.commands.CreateRect
import game.communications.connection.PlayActorConnection
import game.events.Event
import game.events.EventHandler
import game.world.Room
import game.world.physics.Rect
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper

object Player {
  def props( name: String ) = Props( classOf[ Player ], name )

  // Received Messages:
  case object Started extends Event
  case class Moved( mobile: ActorRef, x: Float, y: Float ) extends Event
  case class Invalid( s: String ) extends Event
  case class KeyUp( code: Int ) extends Event
  case class KeyDown( code: Int ) extends Event
  case class Click( x: Int, y: Int ) extends Event

  // Sent Messages
  trait MobileBehavior
  case class Walking( mobile: ActorRef, x: Int ) extends MobileBehavior with Event
  case class Standing( mobile: ActorRef ) extends MobileBehavior with Event
  case object Quit extends Event
}

class Player( val name: String ) extends EventHandler {
  import Player._

  var speed = 5
  var hops = 5
  var dims = Rect( name, 5, 25, 1, 2 )

  /** Represents a RetryingActorConnection */
  var connection: ActorRef = _

  val mobileBehavior: Receive = {
    case Game.NewPlayer( client, room, enumerator, channel ) ⇒
      connection = context.actorOf( PlayActorConnection.props( self, channel ), name = "connection" )
      client ! Game.Connected( connection, enumerator )
      subscribers += room

    case Started ⇒
      connection ! CreateRect( name, dims )
      emit( Room.Arrived( self, dims ) )

    case Room.RoomData( fixtures ) ⇒
      for ( f ← fixtures ) f match {
        case r: Rect ⇒ connection ! game.communications.commands.CreateRect( r.id, r )
        case _       ⇒
      }

    case evt: Moved if evt.mobile == self ⇒
      if ( evt.x != dims.x || evt.y != dims.y ) {
        connection ! game.communications.commands.Move( name, dims.x, dims.y )
      }
      dims = Rect( name, evt.x, evt.y, dims.w, dims.h )

    case Click( x: Int, y: Int ) ⇒
    case KeyUp( 81 )             ⇒ self ! PoisonPill
    case KeyDown( 32 | 38 | 87 ) ⇒
  }

  val standing: Receive = {
    case KeyDown( 65 | 37 ) ⇒ context become movingBehavior( -speed )
    case KeyDown( 68 | 39 ) ⇒ context become movingBehavior( speed )
    case Game.Tick          ⇒ emit( Standing( self ) )
  }

  def moving( speed: Int ): Receive = {
    case KeyUp( 65 | 68 | 37 | 39 ) ⇒ context become standingBehavior
    case Game.Tick                  ⇒ emit( Walking( self, speed ) )
  }

  private def standingBehavior = LoggingReceive { standing orElse mobileBehavior orElse eventHandler }
  private def movingBehavior( speed: Int ) = LoggingReceive { moving( speed ) orElse mobileBehavior orElse eventHandler }

  override def receive: Receive = standingBehavior

  override def postStop {
    logger.info( s"terminated." )
    emit( Quit )
  }

}