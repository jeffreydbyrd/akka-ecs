package controllers

import org.specs2.mutable.Specification
import game.GameModule
import game.EventModule
import game.util.logging.LoggingModule
import akka.io.BackpressureBuffer.Ack
import game.mobile.PlayerModule
import game.world.RoomModule
import game.world.SurfaceModule
import game.communications.ConnectionModule
import game.mobile.MobileModule

class ApplicationSpec
    extends Application
    with Specification
    with EventModule
    with GameModule
    with RoomModule
    with SurfaceModule
    with PlayerModule
    with MobileModule
    with ConnectionModule
    with LoggingModule {

  implicit def system: akka.actor.ActorSystem = null
  val game: akka.actor.ActorRef = null
  val timeout = null

  "getCommand(String)" should {
    val ack = """ {"type" : "ack" , "data" : 42} """
    val ku = """{"type" : "keyup", "data" : 65.0}"""
    val kd = """{"type" : "keydown", "data" : 65}"""
    val clk = """{"type" : "click", "data" : {"x" : 42, "y" : 5}}"""

    val unrec0 = """ {"type" : "keydown", "data" : "STRING"} """
    val unrec1 = """ {"type" : "keydown", "KEY" : 65} """

    val clk1 = """ {"type" : "click", "data" : {"x" : "STRING"}} """

    "return Ack( 42 ) when json = {type: 'ack', data: 42}" in {
      getCommand( ack ) === Ack( 42 )
    }

    "return KeyUp( 65 ) when json = {type : 'keyup', data : 65}" in {
      getCommand( ku ) === KeyUp( 65 )
    }

    "retun KeyDown( 65 ) when json = {type : 'keydown', data : 65}" in {
      getCommand( kd ) === KeyDown( 65 )
    }

    "retun Click( 42, 5 ) when json = {type : 'click', data : {x: 42, y:5}}" in {
      getCommand( clk ) === Click( 42, 5 )
    }

    "return Invalid when the input doesn't follow the spec that getCommand expects" in {
      getCommand( unrec0 ) === Invalid
      getCommand( unrec1 ) === Invalid
    }
  }
}