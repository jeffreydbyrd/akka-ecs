package game.communications

import java.io.Closeable

import game.events.Event

/**
 *  Defines a bidirectional, Closeable service that forwards messages from the Player
 *  to the Client, and from the Client to the Player
 */
trait ConnectionService extends Closeable {
  def toClient( s: String ): Unit
  def toPlayer( e: Event ): Unit
}
