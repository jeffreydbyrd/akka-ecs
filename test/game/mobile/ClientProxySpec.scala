package game.mobile

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuiteLike
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestKit
import akka.testkit.TestProbe
import game.Game

class ClientProxySpec extends TestKit( ActorSystem( "ClientProxySpec" ) )
    with FunSuiteLike
    with BeforeAndAfterAll {
  import game.mobile.ClientProxy._
  import game.world.Room._

  override def afterAll(): Unit = {
    system.shutdown()
  }
}