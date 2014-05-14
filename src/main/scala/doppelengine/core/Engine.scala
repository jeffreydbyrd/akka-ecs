package doppelengine.core

import scala.concurrent.duration.DurationInt

import akka.actor._
import doppelengine.entity.{EntityConfig, Entity}
import akka.util.Timeout
import doppelengine.system.SystemConfig
import doppelengine.core.Updater.Updated

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
  var updaters: Set[ActorRef] = Set()

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

  def updateSystems(): Unit = {
    for (updater <- updaters) updater ! PoisonPill

    updaters =
      for (sys <- systems) yield {
        updaterCount += 1
        context.actorOf(Updater.props(sys, entVersion, entities), s"updater-$updaterCount")
      }
  }

  def onSystemsUpdate(): Unit = {
    sysVersion += 1
    if (sender != context.system.deadLetters)
      sender ! SystemsOpAck(sysVersion)
  }

  def onEntityUpdate(): Unit = {
    entVersion += 1
    updateSystems()
    if (sender != context.system.deadLetters)
      sender ! EntityOpSuccess(entVersion)
  }

  override def receive = {
    case AddSystems(v, configs) if v == sysVersion =>
      systems = systems ++ toSystems(configs)
      updateSystems()
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

    case Updated => updaters -= sender
  }

  override def preStart() = {
    updateSystems()
  }
}