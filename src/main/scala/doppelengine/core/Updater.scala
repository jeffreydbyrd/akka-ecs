package doppelengine.core

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{PoisonPill, ActorRef, Props, Actor}
import doppelengine.entity.Entity
import scala.concurrent.duration._
import doppelengine.system.System.{UpdateAck, UpdateEntities}
import akka.event.LoggingReceive

object Updater {
  def props(system: ActorRef,
            v: Long,
            ents: Set[Entity]) = Props(classOf[Updater], system, v, ents)

  case object Updated

}

class Updater(system: ActorRef, v: Long, ents: Set[Entity]) extends Actor {

  import Updater.Updated

  val update = UpdateEntities(v, ents)

  val schedule = context.system.scheduler.schedule(0.seconds, 300.millis, system, update)

  override def receive: Receive = LoggingReceive {
    case UpdateAck(`v`) =>
      context.parent ! Updated
      self ! PoisonPill
  }

  override def postStop() = {
    schedule.cancel()
  }
}
