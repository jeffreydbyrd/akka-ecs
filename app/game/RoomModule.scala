package game

trait RoomModule extends EventModule {
  this: PlayerModule ⇒

  case class Arrived() extends Event

  class Room( override val id: String ) extends EHRoom

  trait EHRoom extends EventHandler {
    val id: String

    def default: Handle = {
      case Arrived()        ⇒ subscribers = subscribers :+ sender
      case e @ Moved( dir ) ⇒ this emit e
      case _                ⇒
    }

  }

}