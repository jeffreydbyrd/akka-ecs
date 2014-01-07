package game

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import game.AdjustHandler._
import game.EventHandler._

class EventModuleSpec extends TestKit( ActorSystem( "EventModuleSpec" ) )
    with Specification {
  
  case class Test( v: Int ) extends Event

  val adj0: Adjust = { case x ⇒ x }
  val adj1: Adjust = { case Test( i ) ⇒ Test( 2 * i ) }
  val adj2: Adjust = { case Test( i ) ⇒ Test( 3 * i ) }
  val adj3: Adjust = { case Test( i ) if i % 7 == 0 ⇒ Test( 0 ) }

  val adjs = List( adj0, adj1, adj2, adj3 )

  trait TestEventHandler extends EventHandler {
    outgoing = adjs
    def default: Handle = { case _ ⇒ }
  }

  "GenericEventHandler#removeAll" should {
    "remove a subset of Adjusts from 'adjusts'" in {
      new TestEventHandler { def test( as: List[ Adjust ] ) = removeAll( adjs, as ) }
        .test( List( adj0, adj2 ) ) === List( adj1, adj3 )
    }
  }

  "EventHandler#adjust(Event)" should {
    "pipe an Event through a List[ Adjust ] and skip those for which it's not defined at" in {
      new TestEventHandler { def test( e: Event ) = adjust( outgoing, e ) }
        .test( Test( 5 ) ) === Test( 30 )
    }
  }

}