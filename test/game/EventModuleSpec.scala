package game

import org.specs2.mutable.Specification
import akka.actor.ActorSystem

class EventModuleSpec
    extends EventModule
    with Specification {
  
  val system:ActorSystem = ActorSystem("EventModuleSpec")

  case class Test( v: Int ) extends Event

  "GenericEventHandler#remove" should {
    val adj: Adjuster = { case x ⇒ x }
    "remove an Adjuster from 'adjusters' if it is contained within the list" in {
      new GenericEventHandler {
        def default: Handle = { case _ ⇒ }
        protected def emit( e: Event ): Unit = {}
        adjusters = List( adj )
        def test( a: Adjuster ) = remove( a )
      }.test( adj ) === Nil
    }
  }

  "GenericEventHandler#removeAll" should {
    val adj0: Adjuster = { case x ⇒ x }
    val adj1: Adjuster = { case Test( i ) ⇒ Test( 2 * i ) }
    val adj2: Adjuster = { case Test( i ) ⇒ Test( 3 * i ) }
    "remove a subset of Adjusters from 'adjusters'" in {
      new GenericEventHandler {
        def default: Handle = { case _ ⇒ }
        protected def emit( e: Event ): Unit = {}
        adjusters = List( adj0, adj1, adj2 )
        def test( as: List[ Adjuster ] ) = removeAll( as )
      }.test( List( adj0, adj2 ) ) === List( adj1 )
    }
  }

}