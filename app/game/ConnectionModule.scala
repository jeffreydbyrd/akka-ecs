package game

import java.io.Closeable
import play.api.libs.iteratee.Concurrent.Channel

/**
 * Defines a ClientService data structure whose one purpose is to get data to
 * the client and to clean up resources when closed.
 */
trait ConnectionModule {

  /** Defines a Closeable service that sends data of type D to the client */
  trait ClientService extends Closeable {
    def send( data: String ): Unit
  }

  /** A simple service that uses a Play Channel object to get String data to the client */
  class PlayClientService( val c: Channel[ String ] ) extends ClientService {
    override def send( d: String ) = c push d
    override def close = c.eofAndEnd
  }

}