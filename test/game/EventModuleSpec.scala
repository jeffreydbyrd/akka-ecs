package game

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.testkit.TestActorRef

class EventModuleSpec
    extends EventModule
    with Specification {

  implicit val system: ActorSystem = ActorSystem( "EventModuleSpec" )

  val adj0: Adjuster = { case x ⇒ x }
  val adj1: Adjuster = { case Test( i ) ⇒ Test( 2 * i ) }
  val adj2: Adjuster = { case Test( i ) ⇒ Test( 3 * i ) }
  val adj3: Adjuster = { case Test( i ) if i % 7 == 0 ⇒ Test( 0 ) }

  val testAdjusters = List( adj0, adj1, adj2, adj3 )

  case class Test( v: Int ) extends Event

  trait TestEventHandler extends GenericEventHandler {
    adjusters = testAdjusters
    def default: Handle = { case _ ⇒ }
    protected def emit( e: Event ): Unit = {}
  }

  "GenericEventHandler#remove" should {
    "remove an Adjuster from 'adjusters' if it is contained within the list" in {
      new TestEventHandler { def test( a: Adjuster ) = remove( a ) }
        .test( adj0 ) === List( adj1, adj2, adj3 )
    }
  }

  "GenericEventHandler#removeAll" should {
    "remove a subset of Adjusters from 'adjusters'" in {
      new TestEventHandler { def test( as: List[ Adjuster ] ) = removeAll( as ) }
        .test( List( adj0, adj2 ) ) === List( adj1, adj3 )
    }
  }

  "EventHandler#adjust(Event)" should {

    "pipe an Event through a List[ Adjusters ] and skip those for which it's not defined at" in {
      new TestEventHandler { def test( e: Event ) = adjust( e ) }
        .test( Test( 5 ) ) === Test( 30 )
    }
  }

}