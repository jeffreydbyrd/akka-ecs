package game.communications.connection

import RetryingConnection.MessageId
import akka.actor.Actor
import game.events.Event
import game.communications.commands.ClientCommand

object RetryingActorConnection {
  case class Ack( id: MessageId ) extends Event
}

/**
 *  An asynchronous Connection that extends the RetryConnection behavior by receiving
 *  `ToClient(String, Boolean)` messages, caching the String if the Boolean is true,
 *  and forwarding the String to the Client. It also receives `ToPlayer(String)`
 *  messages, but does not implement the toPlayer(String) function.
 */
trait RetryingActorConnection extends Actor with RetryingConnection {
  import RetryingActorConnection._

  var count: MessageId = 0

  def retrying: Receive = {
    case Ack( id ) ⇒ ack( id )
    case e: Event  ⇒ toPlayer( e )
    case cc: ClientCommand ⇒
      val msg = s""" {"id" : $count, "ack":${cc.doCache}, "message" : ${cc.toJson}} """
      if ( cc.doCache ) cache( count, msg )
      toClient( msg )
      count = count + 1
  }
}