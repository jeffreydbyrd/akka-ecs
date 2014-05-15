package doppelengine.system

import akka.actor._
import doppelengine.entity.Entity
import doppelengine.core.operations._
import doppelengine.entity.EntityConfig

object EntityHelper {
  def create(engine: ActorRef, configs: Set[EntityConfig], v: Long = 0) =
    Props(classOf[EntityHelper], engine, true, configs, Set(), v)

  def remove(engine: ActorRef, entities: Set[Entity], v: Long = 0) =
    Props(classOf[EntityHelper], engine, false, Set(), entities, v)

  case class EntityHelperAck(helper: ActorRef)
}

class EntityHelper(engine: ActorRef,
                   adding: Boolean,
                   configs: Set[EntityConfig],
                   entities: Set[Entity],
                   var v: Long) extends Helper(engine) {

  import EntityHelper._

  def command(): Operation =
    if (adding) CreateEntities(v, configs)
    else RemoveEntities(v, entities)

  def onSuccess(): Unit = {
    context.parent ! EntityHelperAck(self)
  }
}
