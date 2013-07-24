package game

import akka.actor.Actor
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.PoisonPill
import akka.actor.actorRef2Scala
import akka.actor.ActorRef

/**
 * Defines all Event driven functionality
 * @author biff
 */
trait EventModule {

  abstract class Event

  /** A function that takes an Event and returns an Event, and also has an internal 'id' */
  trait Adjuster extends ( Event ⇒ Event ) {
    val id: String
  }

  /**
   * This is mostly a convenience object. Use to create Adjusters easily:
   *   val adj = Adjuster( "myadjuster" ) { event =>
   *     //modify event and return new Event
   *   }
   */
  object Adjuster {
    def apply( id0: String ) =
      ( adj: Event ⇒ Event ) ⇒ new Adjuster {
        val id = id0;
        def apply( e: Event ) = adj( e )
      }
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
  trait EventHandler extends GenericEventHandler with akka.actor.Actor {
    type Handler = ActorRef

    override def receive = {
      case e: Event          ⇒ this.handle( e )
      case Subscribe( ar )   ⇒ subscribers = subscribers :+ ar
      case UnSubscribe( ar ) ⇒ subscribers = subscribers.filterNot( _ == ar )
      case Add( as )         ⇒ adjusters = ( adjusters ::: as ).distinct
      case Remove( as )      ⇒ adjusters = EventHandler.this.removeAll( as )
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
    type Handler
    type Handle = Event ⇒ Unit

    /** A list of EventHandlers that subscribe to the Events emitted by this EventHandler */
    var subscribers: List[ Handler ] = _

    /** A list of EventAdjusters that adjust an event before it is emitted */
    var adjusters: List[ Adjuster ] = _

    /**
     * This represents the entry point for all Events into this EventHandler.
     * It handles an Event by either ignoring it, forwarding it, changing internal
     * state, or any combination of the three. This var can also be swapped out
     * for other Handle functions
     */
    var handle: Handle = standard
    def standard: Handle

    /**
     * Pipes an Event through the 'adjusters' list and then sends the
     * end result to each of the 'subscribers'
     */
    protected def emit( e: Event ): Unit

    /**
     * Changes how this EventHandler handles Events by swapping in a new
     * Handle function.
     */
    protected def switchTo( h: Handle ) = this.handle = h

    /**
     * Removes all instances of adjuster 'a' from this.adjusters. Items
     * are matched based on their internal 'id' attributes.
     */
    protected def remove( a: Adjuster ): List[ Adjuster ] =
      GenericEventHandler.this.adjusters.filterNot( _.id == a.id )

    /**
     * Removes all adjusters specified in 'as' from this.adjusters.
     * Items are removed based on their internal 'id' attribute
     */
    protected def removeAll( as: List[ Adjuster ] ): List[ Adjuster ] =
      as.foldLeft( GenericEventHandler.this.adjusters ) { ( ( adjs, a ) ⇒ adjs.filterNot( _.id == a.id ) ) }

  }

}