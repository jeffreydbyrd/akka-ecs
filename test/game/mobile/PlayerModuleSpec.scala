package game.mobile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import org.specs2.mutable.Specification
import akka.actor.ActorRef
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.testkit.TestProbe
import akka.actor.Props

class PlayerModuleSpec extends TestKit( ActorSystem( "PlayerSpec" ) ) with Specification {

//  "When a Player actor is initialized, it" should {
//    "return its own connection ActorRef when I send Start" in {
//      val probe = TestProbe()
//      val plr = system.actorOf( Props( new Player( "" ) ) )
//      probe.send( plr, Player.Start )
//      probe.expectMsg( 500 milli, plr )
//    }
//
//  }
}