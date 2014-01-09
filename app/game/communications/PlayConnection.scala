package game.communications

import play.api.libs.iteratee.Concurrent

/**
 * A simple service that uses a Play Channel object to get String data to the client.
 * It does, however, define how to send data to the Player
 */
trait PlayConnection extends ConnectionService {
  val ( enumerator, channel ) = Concurrent.broadcast[ String ]

  override def toClient( s: String ) = channel push s
  override def close = channel.eofAndEnd
}