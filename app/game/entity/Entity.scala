package game.entity

import akka.actor.ActorRef
import game.components.ComponentType

trait Entity {
  val id: EntityId
  val components: Map[ ComponentType, ActorRef ]

  def hasComponents( types: Iterable[ ComponentType ] ): Boolean =
    types.forall( components.contains )

  def apply( typ: ComponentType ): ActorRef = components( typ )
}