package doppelengine.core.operations

import doppelengine.entity.{Entity, EntityConfig}
import doppelengine.system.SystemConfig
import akka.actor.ActorRef

trait Operation {
  val v: Long
}

trait EntityOp extends Operation {
  override val toString = s"EntityOp-$v"
}

case class CreateEntities(v: Long, props: Set[EntityConfig]) extends EntityOp

case class RemoveEntities(v: Long, es: Set[Entity]) extends EntityOp

trait SystemsOp extends Operation {
  override val toString = s"SystemsOp-$v"
}

case class AddSystems(v: Long, sysConfigs: Set[SystemConfig]) extends SystemsOp

case class RemSystems(v: Long, systems: Set[ActorRef]) extends SystemsOp
