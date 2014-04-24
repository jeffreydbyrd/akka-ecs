package doppelengine.core

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, DurationInt}

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import doppelengine.entity.{EntityConfig, Entity}
import doppelengine.system.{SystemConfig, System}
import doppelengine.component.ComponentType
import akka.util.Timeout

object Engine {
  implicit val timeout = Timeout(1.second)

  def props(sysConfigs: Set[SystemConfig],
            entConfigs: Set[EntityConfig],
            minTickInterval: FiniteDuration) =
    Props(classOf[Engine], sysConfigs, entConfigs, minTickInterval)

  // Received:
  case class SetSystems(props: Set[(Props, String)])

  case object TickAck

  trait EntityOp {
    val v: Long

    override val toString = s"EntityOp-$v"
  }

  case class Add(v: Long, props: Set[EntityConfig]) extends EntityOp

  case class Rem(v: Long, es: Set[Entity]) extends EntityOp

  // sent:
  case class OpAck(v: Long)

  case object Tick

}

class Engine(sysConfigs: Set[SystemConfig],
             entityConfigs: Set[EntityConfig],
             minTickInterval: FiniteDuration) extends Actor {

  import Engine._

  private val systems: Set[ActorRef] =
    for (SystemConfig(prop, id) <- sysConfigs) yield {
      context.actorOf(prop, name = id)
    }

  private def toEntities(configs: Set[EntityConfig]): Set[Entity] = {
    val components: Set[Map[ComponentType, ActorRef]] =
      for (map <- configs) yield
        for {(typ, config) <- map} yield
          typ -> context.actorOf(config.p, config.id)

    for (map <- components) yield
      Entity(map.head._2.path.toString, map)
  }

  override def receive = manage(0, toEntities(entityConfigs))

  private var ready = true

  def manage(version: Long, entities: Set[Entity]): Receive = {
    for (sys <- systems) sys ! System.UpdateEntities(version, entities)
    sender ! OpAck(version)

    LoggingReceive {
      case Tick if !ready => ready = true
      case Tick =>
        ready = false
        val futureAcks = for {sys <- systems} yield sys ? Tick
        Future.sequence(futureAcks).foreach(_ => self ! Tick)
        context.system.scheduler.scheduleOnce(minTickInterval, self, Tick)

      case Add(`version`, configs) =>
        val es = toEntities(configs)
        context.become(manage(version + 1, entities ++ es))

      case Rem(`version`, es) =>
        context.become(manage(version + 1, entities -- es))
        for (e <- es; (_, comp) <- e.components) comp ! PoisonPill

      case op: EntityOp if op.v < version =>
        sender ! System.UpdateEntities(version, entities)
    }
  }

  override def preStart() {
    self ! Tick
  }

}