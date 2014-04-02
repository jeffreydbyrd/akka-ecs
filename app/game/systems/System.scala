package game.systems

import akka.actor.Actor
import game.entity.Entity

object System {
  // Received
  case class UpdateComponents( ents: List[ Entity ] )

  // Sent

}

trait System extends Actor