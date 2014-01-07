package controllers

import org.specs2.mutable.Specification
import akka.io.BackpressureBuffer.Ack

class ApplicationSpec extends Specification {

  import controllers.Application.getCommand
  import game.communications.RetryingConnection
  import game.mobile.Player

  "getCommand(String)" should {
    val ack = """ {"type" : "ack" , "data" : 42} """
    val ku = """{"type" : "keyup", "data" : 65.0}"""
    val kd = """{"type" : "keydown", "data" : 65}"""
    val clk = """{"type" : "click", "data" : {"x" : 42, "y" : 5}}"""

    val unrec0 = """ {"type" : "keydown", "data" : "STRING"} """
    val unrec1 = """ {"type" : "keydown", "KEY" : 65} """

    val clk1 = """ {"type" : "click", "data" : {"x" : "STRING"}} """

    "return Ack( 42 ) when json = {type: 'ack', data: 42}" in {
      getCommand( ack ) === RetryingConnection.Ack( 42 )
    }

    "return KeyUp( 65 ) when json = {type : 'keyup', data : 65}" in {
      getCommand( ku ) === Player.KeyUp( 65 )
    }

    "retun KeyDown( 65 ) when json = {type : 'keydown', data : 65}" in {
      getCommand( kd ) === Player.KeyDown( 65 )
    }

    "retun Click( 42, 5 ) when json = {type : 'click', data : {x: 42, y:5}}" in {
      getCommand( clk ) === Player.Click( 42, 5 )
    }

    "return Invalid when the input doesn't follow the spec that getCommand expects" in {
      getCommand( unrec0 ) === Player.Invalid
      getCommand( unrec1 ) === Player.Invalid
    }
  }
}