package game.mobile

import scala.util.Success
import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import game.ConnectionModule
import game.world.RoomModule
import game.world.SurfaceModule

class PlayerModuleSpec
    extends PlayerModule
    with ConnectionModule
    with RoomModule
    with SurfaceModule
    with Specification {

  implicit val system: ActorSystem = ActorSystem( "PlayerModuleSpec" )

  val NOOP: ClientService[ String ] = new ClientService[ String ] {
    override def send( d: String ) = {}
    override def close {}
  }

  trait Dummy extends GenericPlayer[ String ] {
    val cs: ClientService[ String ] = NOOP
    val name = "dummy"
    var position = Position( 5, 5 )
  }

  "When a Player actor is initialized, it" should {
    "return None when I call setup" in {
      new Dummy { def test = setup }.test must beNone
    }

    "return Connected when I send Start" in {
      { TestActorRef( new Dummy with EHPlayer ) ? Start }.value.get.get === Connected
    }

    "return NotConnected( msg ) when setup returns Some( msg )" in {
      {
        TestActorRef(
          new Dummy with EHPlayer {
            override def setup = Some( "message" )
            def test = setup
          }
        ) ? Start
      }.value.get.get === NotConnected( "message" )
    }

  }

  "getCommand(String)" should {
    val ku = """{"type" : "keyup", "data" : 65.0}"""
    val kd = """{"type" : "keydown", "data" : 65}"""
    val clk = """{"type" : "click", "data" : {"x" : 42, "y" : 5}}"""

    val inv0 = """{'type' : 'keydown', 'data' : 65}"""
    val inv1 = """{"type" : "keydown" "data" : 65}"""
    val inv2 = """{"type" : "keydown", "data" : 65"""

    val unrec0 = """ {"type" : "keydown", "data" : "STRING"} """
    val unrec1 = """ {"type" : "keydown", "KEY" : 65} """

    val clk1 = """ {"type" : "click", "data" : {"x" : "STRING"}} """

    "return KeyUp( 65 ) when json = {type : 'keyup', data : 65}" in {
      getCommand( ku ) === KeyUp( 65 )
    }

    "retun KeyDown( 65 ) when json = {type : 'keydown', data : 65}" in {
      getCommand( kd ) === KeyDown( 65 )
    }

    "retun Click( 42, 5 ) when json = {type : 'click', data : {x: 42, y:5}}" in {
      getCommand( clk ) === Click( 42, 5 )
    }

    "return Invalid('Failed to parse JSON string.') when the input doesn't follow JSON spec" in {
      getCommand( inv0 ) === Invalid( "Failed to parse JSON string." )
      getCommand( inv1 ) === Invalid( "Failed to parse JSON string." )
      getCommand( inv2 ) === Invalid( "Failed to parse JSON string." )
    }

    "return Invalid('Unrecognized command.') when the input doesn't follow the schema" in {
      getCommand( unrec0 ) === Invalid( "Unrecognized command." )
      getCommand( unrec1 ) === Invalid( "Unrecognized command." )
    }

    "return Invalid('A click command expects 'x' and 'y' integer values') when the input " +
      "type is 'click' but 'data' doesn't follow the expected schema" in {
        getCommand( clk1 ) === Invalid( "A click command expects 'x' and 'y' integer values" )
      }
  }

}