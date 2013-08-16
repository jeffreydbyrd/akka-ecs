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
//    List( ceiling, floor, leftWall, rightWall ) foreach {s => adjusters = adjusters :+ s.}
  }

  trait EHRoom extends GenericRoom with EventHandlerActor {
    def default: Handle = {
      case MoveAttempt( xpos, ypos, xdir ) ⇒ this emit Moved( sender, xdir )
      case _                               ⇒
    }

    def canMove( xpos: Int, ypos: Int, xdir: Int ) = {

      false
    }
  }

  class Room( override val id: String ) extends EHRoom
}