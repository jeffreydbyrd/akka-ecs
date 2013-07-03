package models

import play.api.libs.json.JsValue
import play.api.libs.iteratee.Enumerator

object Commands {
  case class Command( msg: JsValue )
  case class Update( json: JsValue )
  case class Start( username: Any )
  case class Connected( out: Enumerator[ JsValue ] )
  case class NotConnected( msg: String )
}