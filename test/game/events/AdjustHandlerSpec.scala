package game.events

import org.scalatest.FunSuiteLike

class AdjustHandlerSpec extends AdjustHandler with FunSuiteLike {
  test( "removeAll should remove a subset of Adjusts from 'adjusts'" ) {
    case class Test( v: Int ) extends Event
    val adj0: Adjust = { case x ⇒ x }
    val adj1: Adjust = { case Test( i ) ⇒ Test( 2 * i ) }
    val adj2: Adjust = { case Test( i ) ⇒ Test( 3 * i ) }
    val adj3: Adjust = { case Test( i ) if i % 7 == 0 ⇒ Test( 0 ) }

    val adjs = List( adj0, adj1, adj2, adj3 )

    removeAll( adjs, List( adj0, adj2 ) ) === List( adj1, adj3 )
  }
}