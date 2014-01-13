package game.events

import AdjustHandler.Add
import AdjustHandler.Remove
import akka.actor.Actor
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.util.logging.AkkaLoggingService
import game.util.logging.LoggingService

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

/**
 * An object that handles Events by either changing internal state, forwarding them,
 * emitting them, or any combination of the three.
 * It is asynchronous and uses Akka Actors and Message passing
 * to handle Events
 */
trait EventHandler extends AdjustHandler with Actor {
  import AdjustHandler._

  val logger: LoggingService = new AkkaLoggingService( this, context )

  val addRemoveAdjusts: Receive = {
    case Add( as )    ⇒ add( as )
    case Remove( as ) ⇒ remove( as )
  }

  /** Pipes an Event through the `adjusters` list and returns the end result */
  private def adjust( e: Event ) =
    outgoing.foldLeft( e ) { ( evt, adj ) ⇒
      adj.applyOrElse( evt, ( _: Event ) ⇒ evt )
    }

  /**
   * Pipes an Event through the 'outgoing' list and then sends the end result to each of the
   * parent and children ActorRefs.
   */
  protected def emit( e: Event ): Unit = {
    val finalEvent = adjust( e )
    for ( s ← context.parent :: context.children.toList )
      s ! finalEvent
  }

}