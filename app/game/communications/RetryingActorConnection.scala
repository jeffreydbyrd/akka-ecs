package game.communications

import RetryingConnection.Ack
import RetryingConnection.MessageId
import RetryingConnection.ToClient
import akka.actor.Actor
import game.events.Event


/**
 *  An asynchronous Connection that extends the RetryConnection behavior by receiving
 *  `ToClient(String, Boolean)` messages, caching the String if the Boolean is true,
 *  and forwarding the String to the Client. It also receives `ToPlayer(String)`
 *  messages, but does not implement the toPlayer(String) function.
 */
trait RetryingActorConnection extends Actor with RetryingConnection {
  import RetryingConnection._

  var count: MessageId = 0

  override def receive = {
    case Ack( id ) ⇒ ack( id )
    case e: Event  ⇒ toPlayer( e )
    case ToClient( json, buff ) ⇒
      val msg = s""" {"id" : $count, "ack":$buff, "message" : $json} """
      if ( buff == true ) cache( count, msg )
      toClient( msg )
      count = count + 1
  }
}