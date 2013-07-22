package game

import akka.actor.Actor
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.PoisonPill
import akka.actor.actorRef2Scala
import akka.actor.ActorRef

trait EventModule {

  abstract class Event

  trait Adjuster extends ( Event ⇒ Event ) {
    val id: String
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
  trait EventHandlerActor extends EventHandler with akka.actor.Actor {
    type Handler = ActorRef

    override def receive = {
      case e: Event          ⇒ this.handle( e )
      case Subscribe( ar )   ⇒ subscribers = subscribers :+ ar
      case UnSubscribe( ar ) ⇒ subscribers = subscribers.filterNot( _ == ar )
      case Add( as )         ⇒ adjusters = ( adjusters ::: adjusters ::: as ).distinct
      case Remove( as )      ⇒ adjusters = this.remove( as )
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
  trait EventHandler {
    type Handler

    /** A list of EventHandlers that subscribe to the Events emitted by this EventHandler */
    var subscribers: List[ Handler ] = _

    /** A list of EventAdjusters that adjust an event before it is emitted */
    var adjusters: List[ Adjuster ] = _

    /**
     * Handles an Event object by either ignoring it, forwarding it,
     * changing internal state, or any combination of the three.
     */
    protected def handle( e: Event ): Unit

    /**
     * Pipes an Event through the 'adjusters' list and then sends the
     * end result to each of the 'subscribers'
     */
    protected def emit( e: Event ): Unit

    /**
     * Removes all adjusters specified in 'as' from this.adjusters.
     * Items are removed based on their internal 'id' attribute
     */
    protected def remove( as: List[ Adjuster ] ): List[ Adjuster ] =
      as.foldLeft( this.adjusters ) { ( ( adjs, a ) ⇒ adjs.filterNot( _.id == a.id ) ) }

  }

}