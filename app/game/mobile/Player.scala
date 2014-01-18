package game.mobile

import scala.math.BigDecimal.int2bigDecimal
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import game.communications.PlayActorConnection
import game.communications.RetryingConnection.Ack
import game.communications.RetryingConnection.ToClient
import game.events.Event
import game.events.EventHandler
import game.events.Handle
import game.world.Room
import game.world.Room.RoomData
import akka.actor.ActorRef
import game.Game
import akka.event.LoggingReceive

object Player {
  def props( name: String ) = Props( classOf[ Player ], name )

  // Received Messages:
  case class Start( room: ActorRef, client: ActorRef )
  case class Moved( mobile: ActorRef, x: Float, y: Float ) extends Event
  case class Invalid( s: String ) extends Event
  case class KeyUp( code: Int ) extends Event
  case class KeyDown( code: Int ) extends Event
  case class Click( x: Int, y: Int ) extends Event

  // Sent Messages
  case class StartResponse( connectionService: ActorRef )
  case class Walking( mobile: ActorRef, x: Int ) extends Event
  case class Standing( mobile: ActorRef ) extends Event
  case object Quit extends Event
}

class Player( val name: String ) extends EventHandler {
  import Player._

  val height = 2
  val width = 1

  var speed = 10
  var hops = 5

  var x: Float = 25
  var y: Float = 100

  /** Represents a RetryingActorConnection */
  val connection = context.actorOf( Props( new PlayActorConnection( self ) ), name = "connection" )

  def move( x: Float, y: Float ) = {
    this.x = x
    this.y = y
  }

  val mobileBehavior: Receive = {
    case Start( room, client ) ⇒
      client ! StartResponse( connection )
      subscribers += room
      room ! Room.Arrived( self, x, y, width, height )
      logger.info( "joined the game" )

    case evt: Moved if evt.mobile == self ⇒
      if ( evt.x != x || evt.y != y )
        connection ! ToClient( s""" { "type":"move", "id":"${self.path}", "position":[${evt.x}, ${evt.y}] } """ )
      move( evt.x, evt.y )

    case RoomData( refs ) ⇒
      for ( ref ← refs )
        connection ! ToClient(
          s""" {"type":"create", "id":"${ref.path}", 
    			"position":[${x},${y}],
    			"dimensions":[$width, $height] } """,
          true )

    case ack: Ack                ⇒ connection ! ack
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