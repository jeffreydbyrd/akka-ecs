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

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test( "Player actor should return its own connection ActorRef when I send Start" ) {
    val probe = TestProbe()
    val plr = system.actorOf( Props( classOf[ Player ], "case1" ), "case1-player" )
    probe.send( plr, Player.Start )
    probe.expectMsgClass( classOf[ ActorRef ] )
  }
}