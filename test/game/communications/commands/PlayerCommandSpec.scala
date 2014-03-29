package game.communications.commands

import org.scalatest.FunSuite
import game.communications.connection.PlayActorConnection

class PlayerCommandSpec extends FunSuite {
  import ServerCommand._

  val ack = """ {"type" : "ack" , "data" : 42} """
  val left = """{"type" : "GO_LEFT" }"""
  val right = """{"type" : "GO_RIGHT" }"""
  val stopLeft = """{"type" : "STOP_LEFT" }"""
  val stopRight = """{"type" : "STOP_RIGHT" }"""
  val clk = """{"type" : "click", "data" : {"x" : 42, "y" : 5}}"""

  test( "getCommand should return Ack( 42 ) when json = {type: 'ack', data: 42}" ) {
    getCommand( ack ) === PlayActorConnection.Ack( 42 )
  }

  test( "getCommand should return GoLeft when json = " + left ) {
    getCommand( left ) === GoLeft
  }

  test( "getCommand should retun GoRight when json = " + right ) {
    getCommand( right ) === GoRight
  }

  test( "getCommand should return GoLeft when json = " + stopLeft ) {
    getCommand( stopLeft ) === StopLeft
  }

  test( "getCommand should retun GoRight when json = " + stopRight ) {
    getCommand( stopRight ) === StopRight
  }

  test( "getCommand should retun Click( 42, 5 ) when json = {type : 'click', data : {x: 42, y:5}}" ) {
    getCommand( clk ) === Click( 42, 5 )
  }

}