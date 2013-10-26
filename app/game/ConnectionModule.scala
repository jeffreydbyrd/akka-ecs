package game

import java.io.Closeable
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Concurrent

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
  class PlayFrameworkClientService extends ClientService {
    val ( enumerator, channel ) = Concurrent.broadcast[ String ]

    override def send( d: String ) = channel push d
    override def close = channel.eofAndEnd
  }

}