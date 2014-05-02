package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import akka.actor.Actor
import doppelengine.entity.Entity
import doppelengine.system.System.UpdateAck
import akka.event.LoggingReceive

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

  def updateEntities(entities:Set[Entity]):Unit

  def onTick():Unit

  override def receive: Receive = LoggingReceive {
    case System.UpdateEntities(v, ents) if v > version =>
      updateEntities(ents)
      sender ! UpdateAck(v)
      version = v

    case Tick =>
      onTick()
      context.system.scheduler.scheduleOnce(tickInterval, self, Tick)
  }

  override def preStart() = {
    if (tickInterval > 0.seconds) self ! Tick
  }
}