package game.entity

import akka.actor.ActorRef
import game.components.ComponentType

class PlayerEntity( override val id: EntityId,
                    inputComp: ActorRef,
                    clientComp: ActorRef ) extends Entity {
  override val components: Map[ ComponentType, ActorRef ] =
    Map(
      ComponentType.Input -> inputComp,
      ComponentType.Client -> clientComp
    )
}