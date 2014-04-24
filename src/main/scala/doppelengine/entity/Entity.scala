package engine.entity

import akka.actor.ActorRef
import engine.component.ComponentType

object Entity {
  def apply(_id: String, _components: Map[ComponentType, ActorRef]) =
    new Entity {
      val id = EntityId(_id)
      val components = _components
    }
}

trait Entity {
  val id: EntityId
  val components: Map[ComponentType, ActorRef]

  def hasComponents(types: Iterable[ComponentType]): Boolean =
    types.forall(components.contains)

  def apply(typ: ComponentType): ActorRef = components(typ)
}