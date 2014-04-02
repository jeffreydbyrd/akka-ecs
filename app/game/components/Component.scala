package game.components

import akka.actor.Actor
import game.entity.EntityId

trait Component extends Actor {
  val id: EntityId
}