package game.mobile

import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.Specification
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import game.communications.ConnectionModule
import game.GameModule
import game.world.RoomModule
import game.world.SurfaceModule
import akka.testkit.TestActorRef

class PlayerModuleSpec
    extends PlayerModule
    with ConnectionModule
    with RoomModule
    with GameModule
    with SurfaceModule
    with Specification {

  implicit val system: ActorSystem = ActorSystem( "PlayerModuleSpec" )
  val GAME = null

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

    "return its own actor ref and connection when I send Start" in {
      val testAr: ActorRef =
        TestActorRef( new Dummy with PlayerEventHandler {
          override val connection = NOOP
        } )
      ( testAr ? Start ).value.get.get === ( testAr, NOOP )
    }

    "return msg:String when setup returns Some( msg )" in {
      {
        TestActorRef(
          new Dummy with PlayerEventHandler {
            override val connection = NOOP
            override def setup = Some( "message" )
            def test = setup
          }
        ) ? Start
      }.value.get.get === "message"
    }

  }
}