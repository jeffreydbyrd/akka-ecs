package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import doppelengine.entity.Entity
import scala.concurrent.duration._
import doppelengine.core.CreateEntities
import doppelengine.entity.EntityConfig
import doppelengine.core.EntityOpSuccess
import doppelengine.core.RemoveEntities
import doppelengine.core.EntityOpFailure

object EntityHelper {
  def create(engine: ActorRef, configs: Set[EntityConfig], v: Long = 0) =
    Props(classOf[EntityHelper], engine, true, configs, Set(), v)

  def remove(engine: ActorRef, entities: Set[Entity], v: Long = 0) =
    Props(classOf[EntityHelper], engine, false, Set(), entities, v)

  case class EntityHelperAck(helper: ActorRef)

  private case object Retry

}

class EntityHelper(engine: ActorRef,
                   adding: Boolean,
                   configs: Set[EntityConfig],
                   entities: Set[Entity],
                   var v: Long) extends Actor {

  import EntityHelper._

  val timer: Cancellable = context.system.scheduler.schedule(0.millis, 100.millis, self, Retry)

  var successful = false

  def attempt(): Unit = {
    if (adding)
      engine ! CreateEntities(v, configs)
    else
      engine ! RemoveEntities(v, entities)
  }

  override def receive: Receive = {
    case Retry if !successful => attempt()

    case EntityOpSuccess(_) if !successful =>
      successful = true
      timer.cancel()
      context.parent ! EntityHelperAck(self)
      self ! PoisonPill

    case EntityOpFailure(correctVersion, _) =>
      v = correctVersion
  }
}
