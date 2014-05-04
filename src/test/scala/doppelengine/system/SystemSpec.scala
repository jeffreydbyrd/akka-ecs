package doppelengine.system

import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{Props, ActorRef, ActorSystem}
import org.scalatest._
import akka.util.Timeout
import scala.concurrent.duration._
import doppelengine.entity.Entity
import doppelengine.system.System.Tick

class SystemSpec
  extends TestKit(ActorSystem("SystemSpec"))
  with FunSuiteLike
  with MustMatchers
  with BeforeAndAfterAll {

  implicit val timeout: Timeout = 1.second

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("System should wait until user defined onTick() is finished before sending another Tick") {
    val probe = TestProbe()
    TestActorRef[TestSystem](Props(classOf[TestSystem], probe.ref, 200.millis))

    probe.expectMsg(Tick)
    probe.expectNoMsg(100.millis)
    probe.expectMsg(Tick)
  }

  test("A System shouldn't Tick if tickInterval <= 0") {
    val probe = TestProbe()
    TestActorRef[TestSystem](Props(classOf[TestSystem], probe.ref, 0.seconds))
    probe.expectNoMsg()
  }
}

class TestSystem(probe: ActorRef, interval: FiniteDuration) extends System(interval) {
  override def updateEntities(entities: Set[Entity]): Unit = {}
  override def onTick(): Unit = probe ! Tick
}