package game

import akka.actor.Actor
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.PoisonPill
import akka.actor.actorRef2Scala
import akka.actor.ActorRef
import akka.actor.ActorSystem

/**
 * Defines all Event driven functionality
 * @author biff
 */
trait EventModule {
  
  val system: ActorSystem

  abstract class Event

  type Adjuster = Event ⇒ Event
  type Handle = PartialFunction[ Event, Unit ]

  implicit def toRichHandle( f: Handle ) = new RichHandle {
    override def apply( e: Event ) = f( e )
    override def isDefinedAt( e: Event ) = f.isDefinedAt( e )
  }

  trait RichHandle extends Handle {
    def ~( that: Handle ): Handle = this orElse that
  }

  // Actor messages:
  case class Subscribe( ar: ActorRef )
  case class UnSubscribe( ar: ActorRef )
  case class Add( as: List[ Adjuster ] )
  case class Remove( as: List[ Adjuster ] )

  /**
   * An asynchronous EventHandler that uses akka Actors and Message passing
   * to handle Events
   * @author biff
   */
  trait EventHandler extends GenericEventHandler with Actor {
    type T = ActorRef

    override def receive = {
      case e: Event          ⇒ this.handle( e )
      case Subscribe( ar )   ⇒ subscribers = subscribers :+ ar
      case UnSubscribe( ar ) ⇒ subscribers = subscribers.filterNot( _ == ar )
      case Add( as )         ⇒ adjusters = ( adjusters ::: as ).distinct
      case Remove( as )      ⇒ adjusters = this.removeAll( as )
      case _                 ⇒
    }

    /**
     * Pipes an Event through the 'adjusters' list and then sends the
     * end result to each of the 'subscribers'
     */
    protected def emit( e: Event ): Unit = {
      val finalEvent = adjusters.foldLeft( e ) { ( evt, adj ) ⇒ adj( evt ) }
      subscribers.foreach { _ ! finalEvent }
    }

  }

  /**
   * An object that handles Events by either changing internal state, forwarding them,
   * emitting them, or any combination of the three.
   * @author biff
   */
  trait GenericEventHandler {
    type T

    /** A list of EventHandlers that subscribe to the Events emitted by this EventHandler */
    var subscribers: List[ T ] = Nil

    /** A list of EventAdjusters that adjust an event before it is emitted */
    var adjusters: List[ Adjuster ] = Nil

    /**
     * This represents the entry point for all Events into this EventHandler.
     * It handles an Event by either ignoring it, forwarding it, changing internal
     * state, or any combination of the three. This var can also be swapped out
     * for other Handle functions
     */
    var handle: Handle = default
    def default: Handle

    /**
     * Pipes an Event through the 'adjusters' list and then sends the
     * end result to each of the 'subscribers'
     */
    protected def emit( e: Event ): Unit

    /**
     * Removes all instances of adjuster 'a' from this.adjusters. Items
     * are matched based on their internal 'id' attributes.
     */
    protected def remove( a: Adjuster ): List[ Adjuster ] =
      this.adjusters.filterNot( _ == a )

    /**
     * Removes all adjusters specified in 'as' from this.adjusters.
     * Items are removed based on their internal 'id' attribute
     */
    protected def removeAll( as: List[ Adjuster ] ): List[ Adjuster ] =
      as.foldLeft( GenericEventHandler.this.adjusters ) { ( ( adjs, a ) ⇒ adjs.filterNot( _ == a ) ) }

  }

}