package game

import scala.util.Success

import org.specs2.mutable.Specification

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper

class PlayerModuleSpec
    extends PlayerModule
    with RoomModule
    with Specification {
  
  implicit val system:ActorSystem = ActorSystem("EventModuleSpec")

  "When a Player actor initializes it" should {
    "return None when I call setup" in {
      new GenericPlayer {
        val name = "test"
        def test = setup
      }.test must beNone
    }

    "return Connected[ Enumerator ] when I send Start()" in {
      val Success( result: Connected ) = { TestActorRef( new Player( "test0" ) ) ? Start() }.value.get
      result.isInstanceOf[ Connected ]
    }

  }

  "getCommand(JsValue)" should {
    val kd: JsValue = Json.obj( "type" -> "keydown", "data" -> 65 )
    val ku: JsValue = Json.obj( "type" -> "keyup", "data" -> 65 )
    val clk = Json.obj(
      "type" -> "click",
      "data" -> Json.obj( "x" -> 42, "y" -> 5 )
    )

    "return KeyUp( 65 ) when json = {type : 'keyup', data : 65}" in {
      getCommand( ku ) === KeyUp( 65 )
    }

    "retun KeyDown( 65 ) when json = {type : 'keydown', data : 65}" in {
      getCommand( kd ) === KeyDown( 65 )
    }

    "retun Click( 42, 5 ) when json = {type : 'click', data : {x: 42, y:5}}" in {
      getCommand( clk ) === Click( 42, 5 )
    }
  }

}