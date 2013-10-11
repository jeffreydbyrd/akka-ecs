package game

import java.io.Closeable
import play.api.libs.iteratee.Concurrent.Channel

trait ConnectionModule {

  /** Defines a closeable service that sends data to the client */
  trait ClientService[ D ] extends Closeable {
    def send( data: D ): Unit
  }

  /** A simple service that uses a Play Channel object to get data to the client */
  class PlayClientService( val c: Channel[ String ] ) extends ClientService[ String ] {
    override def send( d: String ) = c push d
    override def close = c.eofAndEnd
  }

}