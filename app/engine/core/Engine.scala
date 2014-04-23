package engine.core

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import engine.entity.{EntityConfig, Entity}
import engine.system.{SystemConfig, System}
import engine.util.logging.AkkaLoggingService
import engine.component.ComponentType
import akka.util.Timeout

object Engine {
  implicit val timeout = Timeout(1.second)

  def props(sysConfigs: Set[SystemConfig]) =
    Props(classOf[Engine], sysConfigs)

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
  case class EntityOpAck(v: Long)

  case object Tick

}

class Engine(sysConfigs: Set[SystemConfig]) extends Actor {

  import Engine._

  private val logger = new AkkaLoggingService(this, context)

  private val systems: Set[ActorRef] =
    for (SystemConfig(prop, id) <- sysConfigs) yield {
      context.actorOf(prop, name = id)
    }

  private def updateEntities(v: Long, ents: Set[Entity]) = {
    val newV = v + 1
    logger.info(s"Entities v-$newV: $ents")
    for (sys <- systems) {
      sys ! System.UpdateEntities(newV, ents)
      context.become(manage(newV, ents))
    }
    sender ! EntityOpAck(v)
  }

  override def receive = manage(0, Set())

  private var ready = true

  def manage(version: Long, entities: Set[Entity]): Receive = LoggingReceive {
    case Tick if !ready => ready = true
    case Tick =>
      ready = false
      val futureAcks = for {sys <- systems} yield sys ? Tick
      Future.sequence(futureAcks).foreach(_ => self ! Tick)
      context.system.scheduler.scheduleOnce(20 millis, self, Tick)

    case Add(`version`, configs) =>
      val components: Set[Map[ComponentType, ActorRef]] =
        for (map <- configs) yield
          for {(typ, config) <- map} yield
            typ -> context.actorOf(config.p, config.id)

      val es =
        for (map <- components)
        yield Entity(map.head._2.path.toString, map)

      updateEntities(version, entities ++ es)

    case Rem(`version`, es) =>
      updateEntities(version, entities -- es)
      for (e <- es; (_, comp) <- e.components) comp ! PoisonPill

    case op: EntityOp if op.v < version =>
      sender ! System.UpdateEntities(version, entities)
  }

  override def preStart() {
    self ! Tick
  }

}