package doppelengine.entity

import akka.actor.ActorRef
import doppelengine.component.ComponentType

class Entity(val id: EntityId,
             val components: Map[ComponentType, ActorRef]) {

  def hasComponents(types: Iterable[ComponentType]): Boolean =
    types.forall(components.contains)

  def apply(typ: ComponentType): ActorRef = components(typ)
}