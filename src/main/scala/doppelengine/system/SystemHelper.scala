package doppelengine.system

import akka.actor._
import doppelengine.core.operations._

object SystemHelper {
  def add(engine: ActorRef, configs: Set[SystemConfig], v: Long = 0) =
    Props(classOf[SystemHelper], engine, true, configs, Set(), v)

  def rem(engine: ActorRef, systems: Set[ActorRef], v: Long = 0) =
    Props(classOf[SystemHelper], engine, false, Set(), systems, v)

  case class SystemHelperAck(helper: ActorRef)

}

class SystemHelper(engine: ActorRef,
                   adding: Boolean,
                   configs: Set[SystemConfig],
                   systems: Set[ActorRef],
                   var v: Long = 0) extends Helper(engine) {

  import SystemHelper._

  def command(): Operation =
    if (adding) AddSystems(v, configs)
    else RemSystems(v, systems)

  def onSuccess(): Unit = {
    context.parent ! SystemHelperAck(self)
  }
}
