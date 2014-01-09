package game.communications

import java.nio.BufferOverflowException

import game.events.Event

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
