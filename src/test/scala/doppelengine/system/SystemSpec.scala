package doppelengine.system

import scala.concurrent.ExecutionContext.Implicits.global
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{Props, ActorRef, ActorSystem}
import org.scalatest._
import akka.util.Timeout
import scala.concurrent.duration._
import doppelengine.entity.Entity
import scala.concurrent.{Await, Future}
import java.util.Date

class SystemSpec
  extends TestKit(ActorSystem("SystemSpec"))
  with FunSuiteLike
  with BeforeAndAfterAll {

  implicit val timeout: Timeout = 1.second

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("System should wait until user defined onTick() is finished before Ticking again") {
    val probe = TestProbe()
    TestActorRef[TestSystem](Props(classOf[TestSystem], probe.ref, 50.millis))
    probe.expectMsg("tick!")
    probe.expectNoMsg(150.millis)
    probe.expectMsg("tick!")
  }

  test("A System shouldn't Tick if tickInterval <= 0") {
    val probe = TestProbe()
    TestActorRef[TestSystem](Props(classOf[TestSystem], probe.ref, 0.seconds))
    probe.expectNoMsg()
  }
}

class TestSystem(probe: ActorRef, interval: FiniteDuration) extends System(interval) {
  override def updateEntities(entities: Set[Entity]): Unit = {}

  override def onTick(): Unit = {
    val start = (new Date).getTime
    Thread.sleep(200)
    probe ! "tick!"
    println("onTick took " + ((new Date).getTime - start))
  }
}