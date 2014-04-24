package doppelengine.system

import akka.actor.Actor
import doppelengine.entity.Entity

object System {
  // Received
  case class UpdateEntities( version: Long, ents: Set[ Entity ] )
}

trait System extends Actor