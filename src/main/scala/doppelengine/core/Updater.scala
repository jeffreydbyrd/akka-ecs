package doppelengine.core

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import doppelengine.entity.Entity
import scala.concurrent.duration._
import akka.event.LoggingReceive
import doppelengine.system.System.UpdateEntities
import doppelengine.system.System.UpdateAck

object Updater {
  def props(system: ActorRef,
            v: Long,
            ents: Set[Entity]) = Props(classOf[Updater], system, v, ents)
}

class Updater(system: ActorRef, v: Long, ents: Set[Entity]) extends Actor {

  val update = UpdateEntities(v, ents)

  val schedule = context.system.scheduler.schedule(0.seconds, 300.millis, system, update)

  context.watch(system)

  override def receive: Receive = LoggingReceive {
    case UpdateAck(`v`) | Terminated(_) =>
      self ! PoisonPill
  }

  override def postStop() = {
    schedule.cancel()
  }
}
