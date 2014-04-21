package engine.system

import akka.actor.Actor
import engine.entity.Entity

object System {
  // Received
  case class UpdateEntities( version: Long, ents: Set[ Entity ] )
}

trait System extends Actor