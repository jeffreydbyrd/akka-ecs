package game.mobile

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuiteLike
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestKit
import akka.testkit.TestProbe
import game.Game.NewPlayer
import game.Game

class PlayerSpec extends TestKit( ActorSystem( "PlayerSpec" ) )
    with FunSuiteLike
    with BeforeAndAfterAll {
  import game.mobile.Player._
  import game.world.Room._
  import game.events.EventHandler._

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test( "When I send NewPlayer, the Player actor should reply with Connected" ) {
    val client = TestProbe()
    val room = TestProbe()
    val ( enumerator, channel ) = play.api.libs.iteratee.Concurrent.broadcast[ String ]
    val plr = system.actorOf( Props( classOf[ Player ], "case1" ), "case1-player" )
    room.send( plr, Game.NewPlayer( client.ref, room.ref, enumerator, channel ) )

    client.expectMsgClass( classOf[ Game.Connected ] )
  }
}