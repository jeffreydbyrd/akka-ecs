package game

import org.specs2.mutable.Specification
import akka.testkit.TestActorRef
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import akka.testkit.TestActorRef
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import scala.util.Success
import play.api.test.WithApplication
import akka.actor.ActorSystem

class PlayerModuleSpec
    extends PlayerModule
    with Specification {

  implicit val sys = ActorSystem( "testSystem" )

  "When a Player actor initializes, sending Start()" should {
    "return Connected[ Enumerator ]" in {
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