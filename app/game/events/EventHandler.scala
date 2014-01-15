package game.events

import akka.actor.Actor
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.util.logging.AkkaLoggingService
import game.util.logging.LoggingService
import akka.actor.ActorRef

/**
 * An object used to represent a change in the world. EventHandlers use them as immutable
 * messages to interact with each other.
 */
trait Event

/**
 *  An Event that typically represents no change in the world. Generally, it is generated
 *  by an Adjust that negated some other Event.
 */
case object Nullified extends Event

object EventHandler {
  // Received Messages:
  case class AddSubscribers( subscribers: Set[ ActorRef ] )
  case class RemSubscribers( subscribers: Set[ ActorRef ] )
  case class AddAdjusts( as: Set[ Adjust ] )
  case class RemAdjusts( as: Set[ Adjust ] )
}

/**
 * An object that handles Events by either changing internal state, forwarding them,
 * emitting them, or any combination of the three.
 * It is asynchronous and uses Akka Actors and Message passing
 * to handle Events
 */
trait EventHandler extends Actor {
  import EventHandler._

  val logger: LoggingService = new AkkaLoggingService( this, context )

  var subscribers: Set[ ActorRef ] = Set()
  var adjusters: Set[ Adjust ] = Set()

  val eventHandler: Receive = {
    case AddSubscribers( subs ) ⇒ subscribers = subscribers ++ subs
    case RemSubscribers( subs ) ⇒ subscribers = subscribers -- subs
    case AddAdjusts( as )       ⇒ adjusters = adjusters ++ as
    case RemAdjusts( as )       ⇒ adjusters = adjusters -- as
  }

  /** Pipes an Event through the `adjusters` list and returns the end result */
  private def adjust( e: Event ) =
    adjusters.foldLeft( e ) { ( evt, adj ) ⇒
      adj.applyOrElse( evt, ( _: Event ) ⇒ evt )
    }

  /**
   * Pipes an Event through the 'outgoing' list and then sends the end result to each of the
   * parent and children ActorRefs.
   */
  protected def emit( e: Event ): Unit = {
    val finalEvent = adjust( e )
    for ( s ← subscribers ) s ! finalEvent
  }

}