package game.communications.connection

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.communications.commands.ClientCommand
import game.communications.commands.ServerCommand

import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Enumerator

object PlayActorConnection {
  type MessageId = Long

  def props( player: ActorRef, channel: Channel[ String ] ) = Props( classOf[ PlayActorConnection ], player, channel )

  // Received Messages
  case object GetEnum
  case class Ack( id: MessageId ) extends ServerCommand

  // Sent Messages
  case class ReturnEnum( enum: Enumerator[ String ] )
}

class PlayActorConnection( val toServer: ActorRef, val toClient: Channel[ String ] ) extends Actor {
  import ClientCommand.ServerQuit
  import ClientCommand.ServerReady
  import PlayActorConnection._

  var seq: MessageId = 0
  var retryers: Map[ MessageId, ActorRef ] = Map()

  def retry( msg: String ) {
    val prop = Retryer.props( msg, toClient )
    retryers += seq -> context.actorOf( prop, "retryer_" + seq.toString )
  }

  def send( cc: ClientCommand ) = {
    val msg =
      s""" {"seq" : $seq,
            "ack":${cc.doRetry},
            "type": "${cc.typ}",
    	        "message" : ${cc.toJson}} """
    toClient push msg
    if ( cc.doRetry ) {
      retry( msg )
      seq += 1
    }
  }

  /**
   * For a given MessageId, this kills its associated `helper` Actor
   * and removes it from the `helpers` map
   */
  def ack( id: MessageId ) {
    for ( ref <- retryers.get( id ) ) {
      ref ! PoisonPill
    }
    retryers -= id
  }

  override def receive = LoggingReceive {
    case Ack( id )         => ack( id )
    case pc: ServerCommand => toServer ! pc
    case cc: ClientCommand => send( cc )
  }

  override def preStart = {
    send( ServerReady )
  }

  override def postStop {
    send( ServerQuit )
    toClient.eofAndEnd
  }
}