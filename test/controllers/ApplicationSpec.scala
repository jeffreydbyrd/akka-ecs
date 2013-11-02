package controllers

import org.specs2.mutable.Specification
import game.GameModule

class ApplicationSpec extends Application with Specification
    with GameModule {

  implicit def system: akka.actor.ActorSystem = null
  val GAME: akka.actor.ActorRef = null

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