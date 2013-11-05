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
import game.EventModule
import game.GameModule
import game.communications.ConnectionModule
import game.util.logging.LoggingModule
import game.world.RoomModule
import game.world.SurfaceModule

class PlayerModuleSpec
    extends PlayerModule
    with Specification
    with EventModule
    with GameModule
    with RoomModule
    with SurfaceModule
    with MobileModule
    with ConnectionModule
    with LoggingModule {

  implicit val system: ActorSystem = ActorSystem( "PlayerModuleSpec" )
  val game = null
  implicit val timeout = akka.util.Timeout( 500 )

  val NOOP: ActorRef = TestActorRef( new RetryingActorConnection {
    override def toClient( s: String ) = {}
    override def toPlayer( e: Event ) = {}
    override def close = {}
  } )

  trait Dummy extends GenericPlayer {
    val name = "dummy"
    var position = Position( 5, 5, height, width )
  }

  "When a Player actor is initialized, it" should {

    "return its own connection ActorRef when I send Start" in {
      val testAr: ActorRef =
        TestActorRef( new Dummy with PlayerEventHandler {
          override val connection = NOOP
        } )
      ( testAr ? Start ).value.get.get === NOOP
    }

  }
}