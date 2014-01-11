package game.events

import org.scalatest.FunSuiteLike

import akka.actor.ActorSystem
import akka.testkit.TestKit

class EventHandlerSpec extends TestKit( ActorSystem( "EventModuleSpec" ) )
    with FunSuiteLike {

}