package game.mobile

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuiteLike
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestKit
import akka.testkit.TestProbe

class PlayerSpec extends TestKit( ActorSystem( "PlayerSpec" ) )
    with FunSuiteLike
    with BeforeAndAfterAll {
  import game.mobile.Player._
  import game.world.Room._
  import game.events.EventHandler._

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test( "Player actor should return its own connection ActorRef when I send Start" ) {
    val room = TestProbe()
    val client = TestProbe()
    val plr = system.actorOf( Props( classOf[ Player ], "case1" ), "case1-player" )

    room.send( plr, Player.Start( room.ref, client.ref ) )

    client.expectMsgClass( classOf[ StartResponse ] )
    room.expectMsg( Arrived )
  }
}