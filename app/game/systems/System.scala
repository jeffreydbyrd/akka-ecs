package game.systems

import akka.actor.Actor
import game.entity.Entity

object System {
  // Received
  case class UpdateComponents( ents: Set[ Entity ] )
}

trait System extends Actor