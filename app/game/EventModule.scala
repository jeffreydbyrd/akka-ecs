package game

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import game.util.logging.LoggingModule

/**
 * Defines all Event driven functionality. This module uses Akka Actors to
 * encapsulate asynchronous state and behavior, which trickles down through
 * the rest of the application logic.
 */
trait EventModule extends LoggingModule {

  /** The one ActorSystem that the game should use */
  implicit def system: ActorSystem

  /**
   * An object used to represent a change in the world. EventHandlers use them as immutable
   * messages to interact with each other.
   */
  abstract class Event

  /**
   *  An Event that typically represents no change in the world. Generally, it is generated
   *  by an Adjust that negated some other Event.
   */
  case object Nullified extends Event

  /** A PartialFunction used by EventHandlers to modify incoming and outgoing Events */
  type Adjust = PartialFunction[ Event, Event ]
  
  /** A PartialFunction used by EventHandlers to handle certain Events */
  type Handle = PartialFunction[ Event, Unit ]

  // Actor messages:
  /** Tells an EventHandler I want to subscribe to his Events */
  case object Subscribe
  /** Tells an EventHandler I want to unsubscribe from his Events */
  case object Unsubscribe
  /** Tells the target EventHandler to add 'as' to the outgoing list of Adjusts */
  case class AddOut( as: List[ Adjust ] )
  /** Tells the target EventHandler to remove 'as' from the outgoing list of Adjusts */
  case class RemoveOut( as: List[ Adjust ] )
  /** Tells the target EventHandler to add 'as' to the incoming list of Adjusts */
  case class AddIn( as: List[ Adjust ] )
  /** Tells the target EventHandler to remove 'as' from the incoming list of Adjusts */
  case class RemoveIn( as: List[ Adjust ] )

  /**
   * Anything that can adjust events, such as a floor, a piece of armor, or a pair of glasses.
   * These objects can adjust events by applying one or more `Adjust` functions. They may adjust
   * both incoming and outgoing events.
   */
  trait AdjustHandler {
    var incoming: List[ Adjust ] = Nil
    var outgoing: List[ Adjust ] = Nil

    protected def removeAll( current: List[ Adjust ], targets: List[ Adjust ] ): List[ Adjust ] =
      current.filterNot( adj ⇒ targets.contains( adj ) )
  }

  /**
   * An object that handles Events by either changing internal state, forwarding them,
   * emitting them, or any combination of the three.
   */
  trait EventHandler extends AdjustHandler {
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
  }

  /**
   * An asynchronous EventHandler that uses akka Actors and Message passing
   * to handle Events
   */
  trait ActorEventHandler extends EventHandler with Actor {
    val logger: LoggingService = new AkkaLoggingService( this, context )

    /**
     * A list of ActorRefs (ActorEventHandler) that this EventHandler will emit its Events to
     */
    var subscribers: List[ ActorRef ] = Nil

    override def receive = {
      case e: Event        ⇒ this.handle( adjust( incoming, e ) )
      case Subscribe       ⇒ subscribers = subscribers :+ sender
      case Unsubscribe     ⇒ subscribers = subscribers.filterNot( _ == sender )
      case AddOut( as )    ⇒ outgoing = outgoing ::: as
      case AddIn( as )     ⇒ incoming = incoming ::: as
      case RemoveOut( as ) ⇒ outgoing = removeAll( outgoing, as )
      case RemoveIn( as )  ⇒ incoming = removeAll( incoming, as )
      case _               ⇒
    }

    /**
     * Pipes an Event through the 'outgoing' list and then sends the end result to each of the
     * 'subscribers'. If forwarding == true, then the Event is forwarded according to Akka's
     * forwarding documentation:
     * (http://doc.akka.io/docs/akka/snapshot/scala/actors.html#Forward_message)
     */
    protected def emit( e: Event, forwarding: Boolean = false ): Unit = {
      val finalEvent = adjust( outgoing, e )
      for ( s ← subscribers )
        if ( forwarding ) s forward finalEvent
        else s ! finalEvent
    }

  }
}