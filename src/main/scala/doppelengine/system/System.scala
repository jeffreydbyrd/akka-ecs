package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import akka.actor.Actor
import doppelengine.entity.Entity
import doppelengine.system.System.UpdateAck
import java.util.Date

object System {

  // Received
  case class UpdateEntities(version: Long, ents: Set[Entity])

  // Sent
  case class UpdateAck(v: Long)

  case object Tick

}

abstract class System(tickInterval: FiniteDuration) extends Actor {

  import System.Tick

  var version: Long = 0

  def updateEntities(entities: Set[Entity]): Unit

  def onTick(): Unit

  override def receive: Receive = {
    case System.UpdateEntities(v, ents) if v > version =>
      updateEntities(ents)
      sender ! UpdateAck(v)
      version = v

    case Tick =>
      context.system.scheduler.scheduleOnce(tickInterval, self, Tick)
      onTick()
  }

  override def preStart() = {
    if (tickInterval > 0.seconds) self ! Tick
  }
}