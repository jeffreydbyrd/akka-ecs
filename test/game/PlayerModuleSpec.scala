package game

import org.specs2.mutable.Specification
import play.api.libs.json.JsValue
import play.api.libs.json.Json

class PlayerModuleSpec
    extends PlayerModule
    with Specification {

  "getCommand(JsValue)" should {
    val kd: JsValue = Json.obj(
      "type" -> "keydown",
      "data" -> 65
    )

    val ku: JsValue = Json.obj(
      "type" -> "keyup",
      "data" -> 65
    )

    val clk = Json.obj(
      "type" -> "click",
      "data" -> Json.obj(
        "x" -> 42,
        "y" -> 5
      )
    )

    "return KeyUp( 65 ) when json = ku" in {
      getCommand( ku ) === KeyUp( 65 )
    }
  }

}