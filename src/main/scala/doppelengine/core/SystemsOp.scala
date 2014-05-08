package doppelengine.core

import doppelengine.system.SystemConfig
import akka.actor.ActorRef

trait SystemsOp

case class AddSystems(sysConfigs: Set[SystemConfig]) extends SystemsOp

case class RemSystems(systems: Set[ActorRef]) extends SystemsOp

case object SystemsOpAck