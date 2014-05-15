package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{PoisonPill, ActorRef, Cancellable, Actor}
import scala.concurrent.duration._
import doppelengine.core.operations._

object Helper {
  private object Retry
}

abstract class Helper(engine: ActorRef) extends Actor {

  import Helper._

  var v: Long

  val timer: Cancellable = context.system.scheduler.schedule(0.millis, 100.millis, self, Retry)

  var successful = false

  def command(): Operation

  def onSuccess(): Unit

  def attempt(): Unit = {
    engine ! command()
  }

  override def receive: Receive = {
    case Retry if !successful => attempt()

    case _: Ack if !successful =>
      successful = true
      timer.cancel()
      onSuccess()
      self ! PoisonPill

    case f: Failure =>
      v = f.v
  }
}
