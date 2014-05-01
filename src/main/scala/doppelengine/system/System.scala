package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import akka.actor.{Props, Actor}
import doppelengine.entity.Entity
import doppelengine.system.System.UpdateAck
import akka.event.LoggingReceive

object System {

  def props(behavior: SystemBehavior, tickInterval: FiniteDuration) =
    Props(classOf[System], behavior, tickInterval)

  def config(behavior: SystemBehavior, tickInterval: FiniteDuration, name: String) =
    SystemConfig(props(behavior, tickInterval), name)

  // Received
  case class UpdateEntities(version: Long, ents: Set[Entity])

  // Sent
  case class UpdateAck(v: Long)

  case object Tick

}

class System(behavior: SystemBehavior, tickInterval: FiniteDuration) extends Actor {

  import System.Tick

  var version: Long = 0

  override def receive: Receive = LoggingReceive {
    case System.UpdateEntities(v, ents) if v > version =>
      behavior.updateEntities(ents)
      sender ! UpdateAck(v)
      version = v

    case Tick =>
      behavior.onTick()
      context.system.scheduler.scheduleOnce(tickInterval, self, Tick)
  }

  override def preStart() = {
    if (tickInterval > 0.seconds) self ! Tick
  }
}