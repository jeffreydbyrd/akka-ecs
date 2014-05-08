package doppelengine.core

import scala.concurrent.duration.DurationInt

import akka.actor._
import doppelengine.entity.{EntityConfig, Entity}
import doppelengine.component.ComponentType
import akka.util.Timeout
import doppelengine.system.SystemConfig
import doppelengine.core.Updater.Updated
import doppelengine.core.Engine.{SetSystemsFailure, SetSystemsAck, SetSystems}

object Engine {
  implicit val timeout = Timeout(1.second)

  def props(sysConfigs: Set[SystemConfig], entConfigs: Set[EntityConfig]) =
    Props(classOf[Engine], sysConfigs, entConfigs)

  // Received:
  case class SetSystems(v: Long, props: Set[SystemConfig])

  // Sent:
  case class SetSystemsAck(v: Long)

  case class SetSystemsFailure(v: Long)

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

  def toEntities(configs: Set[EntityConfig]): Set[Entity] = {
    val components: Set[Map[ComponentType, ActorRef]] =
      for (map <- configs) yield
        for {(typ, config) <- map} yield
          typ -> context.actorOf(config.p, config.id)

    for (map <- components) yield
      Entity(map.head._2.path.toString, map)
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
    case SetSystems(v, _) if v < sysVersion =>
      sender ! SetSystemsFailure(sysVersion)

    case SetSystems(v, configs) if v == sysVersion =>
      sysVersion += 1
      for (sys <- systems) sys ! PoisonPill
      systems = toSystems(configs)
      updateSystems()
      if (sender != context.system.deadLetters)
        sender ! SetSystemsAck(v)

    case Updated => updaters -= sender

    case CreateEntities(v, configs) if v == entVersion =>
      entities = entities ++ toEntities(configs)
      onEntityUpdate()

    case RemoveEntities(v, es) if v == entVersion =>
      for (e <- es; (_, comp) <- e.components) comp ! PoisonPill
      entities = entities -- es
      onEntityUpdate()

    case op: EntityOp if op.v < entVersion =>
      sender ! EntityOpFailure(entVersion, entities)
  }

  override def preStart() = {
    updateSystems()
  }
}