package game.systems

import akka.actor.Actor
import game.entity.Entity

object System {
  // Received
  case class UpdateEntities( version: Long, ents: Set[ Entity ] )
}

trait System extends Actor