package doppelengine.core

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.FunSuiteLike

class EngineSpec
  extends TestKit(ActorSystem("EngineSpec"))
  with FunSuiteLike {

}
