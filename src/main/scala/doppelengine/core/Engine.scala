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

  // Received:
  case class SetSystems(props: Set[(Props, String)])

  trait EntityOp {
    val v: Long

    override val toString = s"EntityOp-$v"
  }

  case class Add(v: Long, props: Set[EntityConfig]) extends EntityOp

  case class Rem(v: Long, es: Set[Entity]) extends EntityOp

  // sent:
  trait OpAck {
    val v: Long
  }

  case class OpSuccess(v: Long) extends OpAck

  case class OpFailure(v: Long, ents: Set[Entity]) extends OpAck

}

class Engine(sysConfigs: Set[SystemConfig], entityConfigs: Set[EntityConfig])
  extends Actor
  with ActorLogging {

  import Engine._

  var updaters: Set[ActorRef] = Set()

  val systems: Set[ActorRef] =
    for (SystemConfig(prop, id) <- sysConfigs) yield {
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

  var updaterCount = 0

  def updateEntities(v: Long, ents: Set[Entity]): Unit = {
    for (updater <- updaters) updater ! PoisonPill

    updaters =
      for (sys <- systems) yield {
        updaterCount += 1
        context.actorOf(Updater.props(sys, v, ents), s"updater-$updaterCount")
      }
  }

  override def receive = manage(0, toEntities(entityConfigs))

  def manage(version: Long, entities: Set[Entity]): Receive = {
    updateEntities(version, entities)
    if (sender != context.system.deadLetters) sender ! OpSuccess(version)

    {
      case Updated => updaters -= sender

      case Add(`version`, configs) =>
        val es = toEntities(configs)
        context.become(manage(version + 1, entities ++ es))

      case Rem(`version`, es) =>
        for (e <- es; (_, comp) <- e.components) comp ! PoisonPill
        context.become(manage(version + 1, entities -- es))

      case op: EntityOp if op.v < version =>
        sender ! OpFailure(version, entities)
    }
  }
}