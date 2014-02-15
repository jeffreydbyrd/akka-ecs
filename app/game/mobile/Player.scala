package game.mobile

import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.communications.PlayActorConnection
import game.communications.RetryingActorConnection
import game.events.Event
import game.events.EventHandler
import game.world.Room
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.JsObject
import game.world.physics.Fixture

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

  trait MobileBehavior
  case class Walking( mobile: ActorRef, x: Int ) extends MobileBehavior with Event
  case class Standing( mobile: ActorRef ) extends MobileBehavior with Event
  case object Quit extends Event
}

class Player( val name: String ) extends EventHandler {
  import Player._

  val height = 2
  val width = 1

  var speed = 5
  var hops = 5

  var x: Float = 5
  var y: Float = 25

  /** Represents a RetryingActorConnection */
  val connection = context.actorOf( PlayActorConnection.props( self ), name = "connection" )

  val mobileBehavior: Receive = {
    case Start( room, client ) ⇒
      client ! StartResponse( connection )
      val json: JsObject = Json.obj(
        "type" -> "create",
        "id" -> self.path.toString,
        "position" -> Json.arr( x, y ),
        "dimensions" -> Json.arr( width, height )
      )
      connection ! RetryingActorConnection.ToClient( json.toString, true )
      subscribers += room
      room ! Room.Arrived( self, x, y, width, height )

    case Room.RoomData( fixtures ) ⇒
      for ( f ← fixtures ) {
        connection ! RetryingActorConnection.ToClient( Fixture.toJson( f ).toString, true )
      }

    case evt: Moved if evt.mobile == self ⇒
      if ( evt.x != x || evt.y != y ) {
        val json = Json.obj(
          "type" -> "move",
          "id" -> self.path.toString,
          "position" -> Json.arr( evt.x, evt.y )
        )
        connection ! RetryingActorConnection.ToClient( json.toString, false )
      }
      x = evt.x
      y = evt.y

    case Click( x: Int, y: Int )          ⇒
    case KeyUp( 81 )                      ⇒ self ! PoisonPill
    case KeyDown( 32 | 38 | 87 )          ⇒
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