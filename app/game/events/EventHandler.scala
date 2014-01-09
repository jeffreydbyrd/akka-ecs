package game.events

import AdjustHandler.AddIn
import AdjustHandler.AddOut
import AdjustHandler.RemoveIn
import AdjustHandler.RemoveOut
import akka.actor.Actor
import akka.actor.actorRef2Scala
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
  /**
   * Represents the entry point for all Events into this EventHandler.
   * It initially points to 'default' for its behavior, but you can
   * swap in other Handle functions at runtime.
   */
  protected var handle: Handle = default
  protected def default: Handle

  /** Pipes an Event through the `adjusters` list and returns the end result */
  protected def adjust( adjs: List[ Adjust ], e: Event ) =
    adjs.foldLeft( e ) { ( evt, adj ) ⇒
      adj.applyOrElse( evt, ( _: Event ) ⇒ evt )
    }

  val logger: LoggingService = new AkkaLoggingService( this, context )

  override def receive = {
    case e: Event        ⇒ this.handle( adjust( incoming, e ) )
    case AddOut( as )    ⇒ outgoing = outgoing ::: as
    case AddIn( as )     ⇒ incoming = incoming ::: as
    case RemoveOut( as ) ⇒ outgoing = removeAll( outgoing, as )
    case RemoveIn( as )  ⇒ incoming = removeAll( incoming, as )
  }

  /**
   * Pipes an Event through the 'outgoing' list and then sends the end result to each of the
   * parent and children ActorRefs. If forwarding == true, then the Event is forwarded
   * according to Akka's forwarding documentation:
   * (http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Forward_message)
   */
  protected def emit( e: Event, forwarding: Boolean = false ): Unit = {
    val finalEvent = adjust( outgoing, e )
    for ( s ← context.parent :: context.children.toList )
      if ( forwarding ) s forward finalEvent
      else s ! finalEvent
  }

}