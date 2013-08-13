package game

trait ConnectionModule {

  /** Defines a service that sends data to the client */
  trait ClientService[ D ] {
    
    /** Sends a chunk of data to the client */
    def send( data: D ): Unit
    
    /** Closes the connection to the client */
    def close: Unit
  }

}