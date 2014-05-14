package doppelengine.core

import doppelengine.system.SystemConfig
import akka.actor.ActorRef

trait SystemsOp {
  val v: Long
}

case class AddSystems(v: Long, sysConfigs: Set[SystemConfig]) extends SystemsOp

case class RemSystems(v: Long, systems: Set[ActorRef]) extends SystemsOp

case class SystemsOpAck(v: Long)

case class SystemsOpFailure(v: Long)