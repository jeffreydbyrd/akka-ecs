package game

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala

/**
 * Defines all Event driven functionality
 * @author biff
 */
trait EventModule {

  implicit def system: ActorSystem

  abstract class Event

  type Adjust = PartialFunction[ Event, Event ]
  type Handle = PartialFunction[ Event, Unit ]

  implicit def toRichHandle( f: Handle ) = new RichHandle {
    override def apply( e: Event ) = f( e )
    override def isDefinedAt( e: Event ) = f.isDefinedAt( e )
  }

  trait RichHandle extends Handle {
    def ~( that: Handle ): Handle = this orElse that
  }

  // Actor messages:
  case object Subscribe
  case object Unsubscribe
  case class Add( as: List[ Adjust ] )
  case class Remove( as: List[ Adjust ] )

  trait AdjustHandler {
    /** A list of EventAdjusters that adjust an event before it is emitted */
    protected var adjusts: List[ Adjust ] = Nil

    /**
     * Removes all instances of adjuster 'a' from this.adjusters. Items
     * are matched based on their internal 'id' attributes.
     */
    protected def remove( a: Adjust ): List[ Adjust ] =
      this.adjusts.filterNot( _ == a )

    /**
     * Removes all adjusters specified in 'as' from this.adjusters.
     * Items are removed based on their internal 'id' attribute
     */
    protected def removeAll( as: List[ Adjust ] ): List[ Adjust ] =
      as.foldLeft( this.adjusts ) { ( ( adjs, a ) ⇒ adjs.filterNot( _ == a ) ) }

  }

  trait AdjustSupplier extends AdjustHandler {
    def getAdjusts: List[ Adjust ] = adjusts
  }

  /**
   * An object that handles Events by either changing internal state, forwarding them,
   * emitting them, or any combination of the three.
   * @author biff
   */
  trait GenericEventHandler extends AdjustSupplier {
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

    /** Pipes an Event through the 'adjusters' list and returns the end result */
    protected def adjust( e: Event ) =
      adjusts.foldLeft( e ) { ( evt, adj ) ⇒
        if ( adj isDefinedAt evt ) adj( evt ) else evt
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
  trait EventHandlerActor extends GenericEventHandler with Actor {
    type S = ActorRef

    override def receive = {
      case e: Event     ⇒ this.handle( e )
      case Subscribe    ⇒ subscribers = subscribers :+ sender
      case Unsubscribe  ⇒ subscribers = subscribers.filterNot( _ == sender )
      case Add( as )    ⇒ adjusts = ( adjusts ::: as ).distinct
      case Remove( as ) ⇒ adjusts = this.removeAll( as )
      case _            ⇒
    }

    /** Adjust an Event and broadcast the result to the 'subscribers' list */
    protected def emit( e: Event ): Unit = {
      val finalEvent = adjust( e )
      subscribers.foreach { _ ! finalEvent }
    }

  }
}