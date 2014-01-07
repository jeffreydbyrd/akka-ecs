package game.communications

import java.io.Closeable
import java.nio.BufferOverflowException

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import game.EventHandler.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Enumerator

/**
 *  Defines a bidirectional, Closeable service that forwards messages from the Player
 *  to the Client, and from the Client to the Player
 */
trait ConnectionService extends Closeable {
  def toClient( s: String ): Unit
  def toPlayer( e: Event ): Unit
}

/**
 * A simple service that uses a Play Channel object to get String data to the client.
 * It does, however, define how to send data to the Player
 */
trait PlayConnection extends ConnectionService {
  val ( enumerator, channel ) = Concurrent.broadcast[ String ]

  override def toClient( s: String ) = channel push s
  override def close = channel.eofAndEnd
}

object RetryingConnection {
  type MessageId = Long

  case class ToClient( msg: String, buffer: Boolean = false )
  case class Ack( id: MessageId ) extends Event
}

/**
 * A ConnectionService that can account for dropped messages to the Client by buffering a
 * sent message and holding it until it receives a corresponding Ack(id), at which point it
 * removes it. If the buffer grows past the `max` var, then it throws a BufferOverflowException.
 */
trait RetryingConnection extends ConnectionService {
  import RetryingConnection._

  var buffer: Map[ MessageId, String ] = Map.empty
  val max = 100

  /**
   * Removes the message in `buffer` that `id` maps to. Then it attempts to re-send
   * the remaining data in the `buffer` to the Client
   */
  def ack( id: MessageId ) {
    buffer = buffer - id
    retry()
  }

  /**
   * Attempts to re-send the remaining data in the buffer to the Client
   */
  def retry() =
    for ( msg ← buffer.values )
      toClient( msg )

  /** Caches an id mapped to `s` */
  def cache( id: MessageId, s: String ) {
    if ( buffer.size == max ) throw new BufferOverflowException()
    buffer = buffer + ( id -> s )
  }
}

/** Used by the App to deliver a raw JSON formatted string to the Player */

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

object PlayActorConnection {
  case object GetEnum
  case class ReturnEnum( enum: Enumerator[ String ] )
}

/**
 * A RetryingActorConnection that implements toPlayer(String) and also receives
 * `GetEnum` messages, to which it responds by returning a Play! Enumerator[String].
 * This Enumerator[String] produces the Strings that the Player object is pushing
 * to the Connection.
 */
class PlayActorConnection( player: ActorRef ) extends PlayConnection with RetryingActorConnection {
  import PlayActorConnection._

  override def toPlayer( e: Event ) { context.parent ! e }
  def getEnum: Receive = { case GetEnum ⇒ sender ! ReturnEnum( enumerator ) }
  override def receive = getEnum orElse super.receive

  override def postStop {
    toClient( s"""{"id":$count, "message": { "type":"quit", "message":"later!" } } """ )
  }
}