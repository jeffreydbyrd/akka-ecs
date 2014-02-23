package game.communications.connection

import java.io.Closeable
import game.events.Event
import game.communications.commands.PlayerCommand

/**
 *  Defines a bidirectional, Closeable service that forwards messages from the Player
 *  to the Client, and from the Client to the Player
 */
trait ConnectionService extends Closeable {
  def toClient( s: String ): Unit
  def toPlayer( e: PlayerCommand ): Unit
}
