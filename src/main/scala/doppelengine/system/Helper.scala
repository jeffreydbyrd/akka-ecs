package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import scala.concurrent.duration._
import doppelengine.core.operations._
import scala.concurrent.Promise
import doppelengine.entity.{Entity, EntityConfig}

object Helper {

  def addSystemsHelper(engine: ActorRef,
                       p: Promise[Unit],
                       configs: Set[SystemConfig]): Props = {
    val getCommand = (v: Long) => AddSystems(v, configs)
    Props(classOf[Helper], engine, p, getCommand)
  }

  def remSystemsHelper(engine: ActorRef,
                       p: Promise[Unit],
                       systems: Set[ActorRef]): Props = {
    val getCommand = (v: Long) => RemSystems(v, systems)
    Props(classOf[Helper], engine, p, getCommand)
  }

  def addEntityHelper(engine: ActorRef,
                      p: Promise[Unit],
                      configs: Set[EntityConfig],
                      v: Long) = {
    val getCommand = (v: Long) => CreateEntities(v, configs)
    Props(classOf[Helper], engine, p, v, getCommand)
  }

  def remEntityHelper(engine: ActorRef,
                      p: Promise[Unit],
                      entities: Set[Entity],
                      v: Long) = {
    val getCommand = (v: Long) => RemoveEntities(v, entities)
    Props(classOf[Helper], engine, p, getCommand)
  }


  private object Retry

}

class Helper(engine: ActorRef,
             p: Promise[Unit],
             var v: Long = 0,
             val getCommand: Long => Operation) extends Actor {

  import Helper._

  val timer: Cancellable = context.system.scheduler.schedule(0.millis, 100.millis, self, Retry)

  var successful = false

  def attempt(): Unit = {
    engine ! getCommand(v)
  }

  override def receive: Receive = {
    case Retry if !successful => attempt()

    case _: Ack if !successful =>
      successful = true
      timer.cancel()
      p.success({})
      self ! PoisonPill

    case f: Failure =>
      v = f.v
  }
}
