package game

import java.io.Closeable

trait ConnectionModule {

  /** Defines a closeable service that sends data to the client */
  trait ClientService[ D ] extends Closeable {
    def send( data: D ): Unit
  }

}