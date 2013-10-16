package game

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import game.util.logging.LoggingModule

/**
 * Defines all Event driven functionality
 * @author biff
 */
trait EventModule extends LoggingModule {

  implicit def system: ActorSystem

  abstract class Event
  case object Nullified extends Event

  type Adjust = PartialFunction[ Event, Event ]
  type Handle = PartialFunction[ Event, Unit ]

  // Actor messages:
  case object Subscribe
  case object Unsubscribe
  case class AddOut( as: List[ Adjust ] )
  case class RemoveOut( as: List[ Adjust ] )
  case class AddIn( as: List[ Adjust ] )
  case class RemoveIn( as: List[ Adjust ] )

  trait AdjustHandler {
    var incoming: List[ Adjust ] = Nil
    var outgoing: List[ Adjust ] = Nil

    protected def removeAll( current: List[ Adjust ], targets: List[ Adjust ] ): List[ Adjust ] =
      current.filterNot( adj ⇒ targets.contains( adj ) )

  }

  /**
   * An object that handles Events by either changing internal state, forwarding them,
   * emitting them, or any combination of the three.
   * @author biff
   */
  trait EventHandler extends AdjustHandler {
    type S

    /** A list of EventHandlers that subscribe to the Events emitted by this EventHandler */
    protected var subscribers: List[ S ] = Nil

    /**
     * This represents the entry point for all Events into this EventHandler.
     * It handles an Event by either ignoring it, forwarding it, changing internal
     * state, or any combination of the three. This var can also be swapped out
     * for other Handle functions
     */
    protected var handle: Handle = default
    protected def default: Handle

    /** Pipes an Event through the `adjusters` list and returns the end result */
    protected def adjust( adjs: List[ Adjust ], e: Event ) =
      adjs.foldLeft( e ) { ( evt, adj ) ⇒
        adj.applyOrElse( evt, ( _: Event ) ⇒ evt )
      }

    /**
     * Pipes an Event through the 'adjusters' list and then sends the
     * end result to each of the 'subscribers'
     */
    protected def emit( e: Event ): Unit
  }

  /**
   * An asynchronous EventHandler that uses akka Actors and Message passing
   * to handle Events
   * @author biff
   */
  trait ActorEventHandler extends EventHandler with Actor {
    type S = ActorRef
    val logger: LoggingService = new AkkaLoggingService( this, context )

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

    /** Adjust an Event and broadcast the result to the 'subscribers' list */
    protected def emit( e: Event ): Unit = {
      val finalEvent = adjust( outgoing, e )
      subscribers.foreach { _ ! finalEvent }
    }

  }
}