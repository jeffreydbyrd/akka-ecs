package doppelengine.core

import akka.testkit.{TestActorRef, TestProbe, TestKit}
import akka.actor._
import org.scalatest.{MustMatchers, BeforeAndAfterAll, FunSuiteLike}
import scala.concurrent.duration._
import doppelengine.system.SystemConfig
import akka.util.Timeout
import doppelengine.system.System.{UpdateAck, UpdateEntities}
import doppelengine.component.ComponentConfig
import doppelengine.entity.{EntityId, EntityConfig}
import doppelengine.core.operations.{AddSystems, EntityOpSuccess, CreateEntities}
import scala.concurrent.{Await, Future}

class EngineSpec
  extends TestKit(ActorSystem("EngineSpec"))
  with FunSuiteLike
  with MustMatchers
  with BeforeAndAfterAll {

  implicit val timeout: Timeout = 1.second

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("Engine should update Systems on instantiation") {
    val probe = TestProbe()
    val props: Props = Props(classOf[ForwardingActor], probe.ref)
    val sysConfigs = Set(SystemConfig(props, "forwarding-system-1"))
    TestActorRef[Engine](Props(classOf[Engine], sysConfigs, Set()))
    probe.expectMsg(UpdateEntities(0, Set()))
  }

  test("Engine should update Systems when I Add a component") {
    val systemProbe = TestProbe()
    val componentProbe = TestProbe()
    systemProbe.ignoreMsg {
      case UpdateEntities(_, ents) if ents.size == 0 => true
      case _ => false
    }

    val sysProps: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val comProps: Props = Props(classOf[ForwardingActor], componentProbe.ref)

    val sysConfigs = Set(SystemConfig(sysProps, "forwarding-system-2"))
    val engine = TestActorRef[Engine](Props(classOf[Engine], sysConfigs, Set()))

    val compConfig = ComponentConfig(comProps, "forwarding-component-2")
    val entityConfig: EntityConfig = EntityConfig(EntityId("id-2"), Map(TestComponent -> compConfig))

    val probe = TestProbe()
    probe.send(engine, CreateEntities(0, Set(entityConfig)))
    probe.expectMsg(EntityOpSuccess(1))

    systemProbe.expectMsgPF() {
      case ue@UpdateEntities(1, ents) if ents.size > 0 => ue
    }
  }

  test("Engine should retry UpdateEntities until it receives an UpdateAck") {
    val systemProbe = TestProbe()
    val props: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val sysConfigs = Set(SystemConfig(props, "forwarding-system-4"))

    val engine = TestActorRef[Engine](Props(classOf[Engine], sysConfigs, Set()), "test-engine-4")

    systemProbe.expectMsg(UpdateEntities(0, Set()))
    systemProbe.expectMsg(UpdateEntities(0, Set()))
    systemProbe.expectMsg(UpdateEntities(0, Set()))

    val fUpdater: Future[ActorRef] = system.actorSelection(engine.path / "updater-1").resolveOne()
    val updater: ActorRef = Await.result(fUpdater, 1.second)
    updater ! UpdateAck(0)

    systemProbe.expectNoMsg(500 millis)
  }

  test("Engine should send UpdateEntities to added Systems") {
    val engine = TestActorRef[Engine](Props(classOf[Engine], Set(), Set()))

    val systemProbe = TestProbe()
    val props: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val sysConfigs = Set(SystemConfig(props, "forwarding-system-8"))

    engine ! AddSystems(0, sysConfigs)
    systemProbe.expectMsg(UpdateEntities(0, Set()))
  }

}
