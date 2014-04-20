package engine.communications.connection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.Props
import akka.event.LoggingReceive
import play.api.libs.iteratee.Concurrent.Channel

object Retryer {
  def props( msg: String, channel: Channel[ String ] ) = Props( classOf[ Retryer ], msg, channel )

  case object Retry
}

class Retryer( msg: String, channel: Channel[ String ] ) extends Actor {
  import Retryer._

  val retry = context.system.scheduler.schedule( 1000 millis, 1000 millis, self, Retry )

  override def receive = LoggingReceive {
    case Retry => channel push msg
  }

  override def postStop = {
    retry.cancel()
  }
}