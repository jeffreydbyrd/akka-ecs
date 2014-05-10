package doppelengine.core

import scala.concurrent.duration.DurationInt

import akka.actor._
import doppelengine.entity.{EntityConfig, Entity}
import doppelengine.component.ComponentType
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

  def onEntityUpdate(): Unit = {
    entVersion += 1
    updateSystems()
    if (sender != context.system.deadLetters)
      sender ! EntityOpSuccess(entVersion)
  }

  override def receive = {
    case AddSystems(configs) =>
      systems = systems ++ toSystems(configs)
      updateSystems()
      if (sender != context.system.deadLetters)
        sender ! SystemsOpAck

    case RemSystems(refs) =>
      systems = systems -- refs
      if (sender != context.system.deadLetters)
        sender ! SystemsOpAck

    case Updated => updaters -= sender

    case CreateEntities(v, configs) if v == entVersion =>
      entities = entities ++ toEntities(configs)
      onEntityUpdate()

    case RemoveEntities(v, es) if v == entVersion =>
      entities = entities -- es
      onEntityUpdate()

    case op: EntityOp if op.v < entVersion =>
      sender ! EntityOpFailure(entVersion, entities)
  }

  override def preStart() = {
    updateSystems()
  }
}