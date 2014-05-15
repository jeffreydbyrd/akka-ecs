package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import akka.actor.{Props, ActorRef, Actor}
import doppelengine.entity.{EntityConfig, Entity}
import doppelengine.system.System.UpdateAck
import scala.concurrent.{Promise, Future}

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

  def addSystems(engine: ActorRef, configs: Set[SystemConfig]): Future[Unit] = {
    val p: Promise[Unit] = Promise()
    val props: Props = Helper.addSystemsHelper(engine, p, configs)
    context.actorOf(props)
    p.future
  }

  def remSystems(engine: ActorRef, systems: Set[ActorRef]): Future[Unit] = {
    val p: Promise[Unit] = Promise()
    val props: Props = Helper.remSystemsHelper(engine, p, systems)
    context.actorOf(props)
    p.future
  }

  def createEntities(engine: ActorRef, configs: Set[EntityConfig]): Future[Unit] = {
    val p: Promise[Unit] = Promise()
    val props: Props = Helper.addEntityHelper(engine, p, configs, version)
    context.actorOf(props)
    p.future
  }

  def removeEntities(engine: ActorRef, ents: Set[Entity]): Future[Unit] = {
    val p: Promise[Unit] = Promise()
    val props: Props = Helper.remEntityHelper(engine, p, ents, version)
    context.actorOf(props)
    p.future
  }

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