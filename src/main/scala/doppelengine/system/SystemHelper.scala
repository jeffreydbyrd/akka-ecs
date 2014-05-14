package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import doppelengine.core._
import doppelengine.core.AddSystems
import doppelengine.core.RemSystems
import scala.concurrent.duration._

object SystemHelper {
  def add(engine: ActorRef, configs: Set[SystemConfig]) =
    Props(classOf[SystemHelper], engine, true, configs, Set())

  def rem(engine: ActorRef, systems: Set[ActorRef]) =
    Props(classOf[SystemHelper], engine, false, Set(), systems)

  private case object Retry

  case class SystemHelperAck(helper: ActorRef)

}

class SystemHelper(engine: ActorRef,
                   adding: Boolean,
                   configs: Set[SystemConfig],
                   systems: Set[ActorRef]) extends Actor {

  import SystemHelper._

  var v: Long = 0

  val timer: Cancellable = context.system.scheduler.schedule(0.millis, 100.millis, self, Retry)

  var successful = false

  def attempt(): Unit = {
    if (adding)
      engine ! AddSystems(v, configs)
    else
      engine ! RemSystems(v, systems)
  }

  override def receive: Receive = {
    case Retry if !successful => attempt()

    case SystemsOpAck(_) if !successful =>
      successful = true
      timer.cancel()
      context.parent ! SystemHelperAck(self)
      self ! PoisonPill

    case SystemsOpFailure(currentV) =>
      v = currentV
  }
}
