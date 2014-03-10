package game.mobile

import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.Game
import game.communications.commands._
import game.communications.connection.PlayActorConnection
import game.events.Event
import game.events.EventHandler
import game.world.Room
import game.world.physics.Rect

object Player {
  def props( name: String ) = Props( classOf[ Player ], name )

  // Received Messages:
  case class Moved( mobile: ActorRef, x: Float, y: Float ) extends Event

  // Sent Messages
  trait MoveAttempt extends Event
  case class WalkAttempt( mobile: ActorRef, x: Int ) extends MoveAttempt
  case class JumpAttempt( mobile: ActorRef, f: Int ) extends MoveAttempt
  case class Quit( mob: ActorRef ) extends Event
  case class PlayerData( mobile: ActorRef, dims: Rect )
}

class Player( val name: String ) extends EventHandler {
  import Player._

  var speed = 5
  var hops = 10
  var dimensions = Rect( name, 5, 25, 1, 2 )

  var connection: ActorRef = _

  val coreBehavior: Receive = {
    case Game.NewPlayer( client, room, enumerator, channel ) ⇒
      connection = context.actorOf( PlayActorConnection.props( self, channel ), name = "connection" )
      client ! Game.Connected( connection, enumerator )
      subscribers += room
      logger.info( "Connected" )

    case Room.Arrived( mobile, dims ) ⇒
      mobile ! PlayerData( self, dimensions )
      connection ! CreateRect( mobile.path.toString, dims, true )

    case Player.PlayerData( mobile, rect ) if mobile != self ⇒
      connection ! CreateRect( mobile.path.toString, rect, true )

    case Room.RoomData( fixtures ) ⇒ for ( f ← fixtures ) f match {
      case r: Rect ⇒ connection ! game.communications.commands.CreateRect( r.id, r, true )
    }

    case Moved( mob, x, y ) ⇒
      connection ! game.communications.commands.Move( mob.path.toString, x, y )
      if ( mob == self )
        dimensions = Rect( name, x, y, dimensions.w, dimensions.h )

    case Started ⇒
      logger.info( "received Started" )
      emit( Room.Arrived( self, dimensions ) )

    case game.communications.commands.Quit ⇒ self ! PoisonPill
    case Click( x: Int, y: Int )           ⇒
    case Jump                              ⇒ emit( JumpAttempt( self, hops ) )
  }

  def moving( s: Int, goLeft: Boolean, goRight: Boolean ): Receive = ( {
    case Game.Tick            ⇒ emit( WalkAttempt( self, s ) )
    case GoLeft if !goLeft    ⇒ context become ( moving( s - speed, true, goRight ) )
    case StopLeft if goLeft   ⇒ context become ( moving( s + speed, false, goRight ) )
    case GoRight if !goRight  ⇒ context become ( moving( s + speed, goLeft, true ) )
    case StopRight if goRight ⇒ context become ( moving( s - speed, goLeft, false ) )
  }: Receive ) orElse coreBehavior orElse eventHandler

  override def receive: Receive = moving( 0, false, false )

  override def postStop {
    logger.info( s"terminated." )
    emit( Quit( self ) )
  }

}