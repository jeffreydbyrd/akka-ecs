package game.communications

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import game.events.Event
import play.api.libs.iteratee.Enumerator

object PlayActorConnection {
  case object GetEnum
  case class ReturnEnum( enum: Enumerator[ String ] )
}

/**
 * A RetryingActorConnection that implements toPlayer(String) and also receives
 * `GetEnum` messages, to which it responds by returning a Play! Enumerator[String].
 * This Enumerator[String] produces the Strings that the Player object is pushing
 * to the Connection.
 */
class PlayActorConnection( player: ActorRef ) extends PlayConnection with RetryingActorConnection {
  import PlayActorConnection._

  override def toPlayer( e: Event ) { context.parent ! e }
  def getEnum: Receive = { case GetEnum ⇒ sender ! ReturnEnum( enumerator ) }
  override def receive = getEnum orElse super.receive

  override def postStop {
    toClient( s"""{"id":$count, "message": { "type":"quit", "message":"later!" } } """ )
  }
}