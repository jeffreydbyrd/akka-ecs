package game.world

import akka.actor.ActorRef
import game.EventModule
import game.mobile.PlayerModule

trait RoomModule extends EventModule {
  this: PlayerModule with SurfaceModule ⇒

  case object Arrived extends Event
  case class Moved( ar: ActorRef, xdir: Int ) extends Event

  // All rooms in the game are equipped with the same 4 surrounding surfaces:
  val ceiling: DoubleSided = DoubleSided( 50, 100, 100, Flat )
  val floor: DoubleSided = DoubleSided( 50, 0, 100, Flat )
  val leftWall: Wall = Wall( 0, 50, 100 )
  val rightWall: Wall = Wall( 100, 50, 100 )

  trait GenericRoom {
    val id: String
  }

  trait EHRoom extends GenericRoom with EventHandler {
    def default: Handle = {
      case MoveAttempt( xdir ) ⇒
      	//TODO: Do some surface logic here...
        this emit Moved( sender, xdir )
      case _ ⇒
    }
  }

  class Room( override val id: String ) extends EHRoom
}