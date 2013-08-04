package game

trait RoomModule extends EventModule {
  this: PlayerModule ⇒
  
  class Room( override val id: String ) extends EHRoom

  trait EHRoom extends EventHandler {
    val id: String

    def default: Handle = {
      
      case _ ⇒
    }

  }

}