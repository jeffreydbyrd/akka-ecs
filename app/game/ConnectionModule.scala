package game

trait ConnectionModule {

  /** Defines a service that sends data to the client */
  trait ClientService[ D ] {
    def send( data: D ): Unit
  }

}