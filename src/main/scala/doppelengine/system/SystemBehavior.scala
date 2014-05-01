package doppelengine.system

import doppelengine.entity.Entity

trait SystemBehavior {
  def updateEntities(entities: Set[Entity]): Unit

  def onTick(): Unit
}
