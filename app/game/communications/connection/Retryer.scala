package game.communications.connection

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import play.api.libs.iteratee.Concurrent.Channel
import akka.actor.Props
import scala.concurrent.duration._
import game.util.logging.AkkaLoggingService
import akka.event.LoggingReceive

object Retryer {
  def props( msg: String, channel: Channel[ String ] ) = Props( classOf[ Retryer ], msg, channel )

  case object Retry
}

class Retryer( msg: String, channel: Channel[ String ] ) extends Actor {
  import Retryer._

  val logger = new AkkaLoggingService( this, context )
  val retry = context.system.scheduler.schedule( 100 millis, 100 millis, self, Retry )

  override def receive = LoggingReceive {
    case Retry ⇒
      channel push msg
  }

}