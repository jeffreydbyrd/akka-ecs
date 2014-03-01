package game.communications.connection

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import game.events.Event
import play.api.libs.iteratee.Enumerator
import akka.actor.Props
import play.api.libs.iteratee.Concurrent.Channel
import game.communications.commands.PlayerCommand
import akka.actor.Actor
import akka.actor.PoisonPill
import game.communications.commands.ClientCommand
import akka.event.LoggingReceive

object PlayActorConnection {
  type MessageId = Long

  def props( player: ActorRef, channel: Channel[ String ] ) = Props( classOf[ PlayActorConnection ], player, channel )

  // Received Messages
  case object GetEnum
  case class Ack( id: MessageId ) extends PlayerCommand

  // Sent Messages
  case class ReturnEnum( enum: Enumerator[ String ] )
}

class PlayActorConnection( val player: ActorRef, val channel: Channel[ String ] ) extends Actor {
  import PlayActorConnection._

  // start at 1 b/c application sent a message already
  var seq: MessageId = 1
  var retryers: Map[ MessageId, ActorRef ] = Map()

  def retry( c: MessageId, msg: String ) {
    val prop = Retryer.props( msg, channel )
    retryers += seq -> context.actorOf( prop, "retryer_" + seq.toString )
  }

  def send( msg: String ) = channel push msg

  /**
   * For a given MessageId, this kills its associated `helper` Actor
   * and removes it from the `helpers` map
   */
  def ack( id: MessageId ) {
    for ( ref ← retryers.get( id ) ) {
      ref ! PoisonPill
    }
    retryers -= id
  }

  override def receive = LoggingReceive {
    case Ack( id )         ⇒ ack( id )
    case pc: PlayerCommand ⇒ context.parent ! pc
    case cc: ClientCommand ⇒
      val msg = s""" {"seq" : $seq, "ack":${cc.doRetry}, "message" : ${cc.toJson}} """
      send( msg )
      if ( cc.doRetry ) {
        retry( seq, msg )
        seq += 1
      }
  }

  override def postStop {
    send( s"""{"seq":$seq, "ack":false, "message": { "type":"quit", "message":"later!" } } """ )
    channel.eofAndEnd
  }
}