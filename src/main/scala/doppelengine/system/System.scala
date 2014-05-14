package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import akka.actor.{Props, ActorRef, Actor}
import doppelengine.entity.{EntityConfig, Entity}
import doppelengine.system.System.UpdateAck
import doppelengine.system.EntityHelper.EntityHelperAck
import doppelengine.system.SystemHelper.SystemHelperAck

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
  var entityHelpers: Set[ActorRef] = Set()
  var systemHelpers: Set[ActorRef] = Set()

  def updateEntities(entities: Set[Entity]): Unit

  def onTick(): Unit

  def addSystems(engine: ActorRef, configs: Set[SystemConfig]): Unit = {
    val props: Props = SystemHelper.add(engine, configs)
    systemHelpers += context.actorOf(props)
  }

  def remSystems(engine: ActorRef, configs: Set[ActorRef]): Unit = {
    val props: Props = SystemHelper.rem(engine, configs)
    systemHelpers += context.actorOf(props)
  }

  def createEntities(engine: ActorRef, configs: Set[EntityConfig]): Unit = {
    val props: Props = EntityHelper.create(engine, configs, version)
    entityHelpers += context.actorOf(props)
  }

  def removeEntities(engine: ActorRef, ents: Set[Entity]): Unit = {
    val props: Props = EntityHelper.remove(engine, ents, version)
    entityHelpers += context.actorOf(props)
  }

  override def receive: Receive = {
    case System.UpdateEntities(v, ents) if v > version =>
      updateEntities(ents)
      sender ! UpdateAck(v)
      version = v

    case Tick =>
      context.system.scheduler.scheduleOnce(tickInterval, self, Tick)
      onTick()

    case EntityHelperAck(helper) => entityHelpers -= helper
    case SystemHelperAck(helper) => systemHelpers -= helper
  }

  override def preStart() = {
    if (tickInterval > 0.seconds) self ! Tick
  }
}