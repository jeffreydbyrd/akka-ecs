package doppelengine.core

import scala.concurrent.duration.DurationInt

import akka.actor._
import doppelengine.entity.Entity
import akka.util.Timeout
import doppelengine.core.operations._
import doppelengine.core.operations.EntityOpSuccess
import doppelengine.core.operations.RemSystems
import doppelengine.core.operations.SystemsOpAck
import doppelengine.core.operations.AddSystems
import doppelengine.entity.EntityConfig
import doppelengine.system.SystemConfig

object Engine {
  implicit val timeout = Timeout(1.second)

  def props(sysConfigs: Set[SystemConfig], entConfigs: Set[EntityConfig]) =
    Props(classOf[Engine], sysConfigs, entConfigs)
}

class Engine(sysConfigs: Set[SystemConfig], entityConfigs: Set[EntityConfig])
  extends Actor
  with ActorLogging {

  var systems: Set[ActorRef] = toSystems(sysConfigs)
  var entities: Set[Entity] = toEntities(entityConfigs)

  var sysVersion: Long = 0
  var entVersion: Long = 0

  var updaterCount = 0

  def toSystems(configs: Set[SystemConfig]): Set[ActorRef] =
    for (SystemConfig(prop, id) <- configs) yield {
      context.actorOf(prop, name = id)
    }

  def toEntities(configs: Set[EntityConfig]): Set[Entity] =
    for (entConfig <- configs) yield {
      val components =
        for {(typ, compConfig) <- entConfig.components} yield
          typ -> context.actorOf(compConfig.p, compConfig.id)
      new Entity(entConfig.id, components)
    }

  def onSystemsUpdate(): Unit = {
    sysVersion += 1
    if (sender != context.system.deadLetters)
      sender ! SystemsOpAck(sysVersion)
  }

  def sendOutUpdates(_systems: Set[ActorRef]) = {
    for (sys <- _systems) {
      updaterCount += 1
      context.actorOf(Updater.props(sys, entVersion, entities), s"updater-$updaterCount")
    }

  }

  def onEntityUpdate(): Unit = {
    entVersion += 1
    sendOutUpdates(systems)
    if (sender != context.system.deadLetters)
      sender ! EntityOpSuccess(entVersion)
  }

  override def receive = {
    case AddSystems(v, configs) if v == sysVersion =>
      val newSystems: Set[ActorRef] = toSystems(configs)
      systems = systems ++ newSystems
      sendOutUpdates(newSystems)
      onSystemsUpdate()

    case RemSystems(v, refs) if v == sysVersion =>
      systems = systems -- refs
      onSystemsUpdate()

    case CreateEntities(v, configs) if v == entVersion =>
      entities = entities ++ toEntities(configs)
      onEntityUpdate()

    case RemoveEntities(v, es) if v == entVersion =>
      entities = entities -- es
      onEntityUpdate()

    case op: SystemsOp if op.v < sysVersion =>
      sender ! SystemsOpFailure(sysVersion)

    case op: EntityOp if op.v < entVersion =>
      sender ! EntityOpFailure(entVersion, entities)
  }

  override def preStart() = {
    sendOutUpdates(systems)
  }
}