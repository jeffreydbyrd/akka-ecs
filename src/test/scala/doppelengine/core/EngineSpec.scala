package doppelengine.core

import akka.testkit.{TestActorRef, TestProbe, TestKit}
import akka.actor._
import org.scalatest.{MustMatchers, BeforeAndAfterAll, FunSuiteLike}
import scala.concurrent.duration._
import doppelengine.system.SystemConfig
import akka.util.Timeout
import doppelengine.system.System.{UpdateAck, UpdateEntities}
import doppelengine.component.ComponentConfig
import doppelengine.entity.{EntityId, Entity, EntityConfig}
import scala.concurrent.Await

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

  test("Engine should terminate components when Removed") {
    val systemProbe = TestProbe()
    val componentProbe = TestProbe()

    val sysProps: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val comProps: Props = Props(classOf[ForwardingActor], componentProbe.ref)

    val sysConfigs = Set(SystemConfig(sysProps, "forwarding-system-3"))
    val compConfig = ComponentConfig(comProps, "forwarding-component-3")
    val entityConfigs: Set[EntityConfig] = Set(
      EntityConfig(EntityId("id-3"), Map(TestComponent -> compConfig))
    )
    val engine = TestActorRef[Engine](Props(classOf[Engine], sysConfigs, entityConfigs))

    val f = system.actorSelection(engine.path / "forwarding-component-3").resolveOne
    val compRef = Await.result(f, 1.second)
    componentProbe.watch(compRef)

    val ents: Set[Entity] =
      systemProbe.expectMsgPF() {
        case ue@UpdateEntities(0, es) if es.size == 1 => es
      }

    val probe = TestProbe()
    probe.send(engine, RemoveEntities(0, ents))
    probe.expectMsg(EntityOpSuccess(1))

    componentProbe.expectTerminated(compRef)
  }

  test("Engine should retry UpdateEntities until it receives an UpdateAck") {
    val systemProbe = TestProbe()
    val props: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val sysConfigs = Set(SystemConfig(props, "forwarding-system-4"))

    val engine = TestActorRef[Engine](Props(classOf[Engine], sysConfigs, Set()), "test-engine-4")
    engine.underlyingActor.updaters.size mustBe 1

    systemProbe.expectMsg(UpdateEntities(0, Set()))
    systemProbe.expectMsg(UpdateEntities(0, Set()))
    systemProbe.expectMsg(UpdateEntities(0, Set()))

    engine.underlyingActor.updaters.head ! UpdateAck(0)

    systemProbe.expectNoMsg(500 millis)
    engine.underlyingActor.updaters.size mustBe 0
  }

  test("Engine should retry UpdateEntities until another Update cancels it") {
    val systemProbe = TestProbe()
    val componentProbe = TestProbe()

    val sysProps: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val comProps: Props = Props(classOf[ForwardingActor], componentProbe.ref)

    val sysConfigs = Set(SystemConfig(sysProps, "forwarding-system-5"))
    val engine = TestActorRef[Engine](Props(classOf[Engine], sysConfigs, Set()))

    val compConfig = ComponentConfig(comProps, "forwarding-component-5")
    val entityConfig: EntityConfig = EntityConfig(EntityId("id-5"), Map(TestComponent -> compConfig))

    engine.underlyingActor.updaters.size mustBe 1

    systemProbe.expectMsg(UpdateEntities(0, Set()))
    systemProbe.expectMsg(UpdateEntities(0, Set()))
    systemProbe.expectMsg(UpdateEntities(0, Set()))

    val probe = TestProbe()
    probe.send(engine, CreateEntities(0, Set(entityConfig)))
    probe.expectMsg(EntityOpSuccess(1))

    systemProbe.expectMsgPF() {
      case ue: UpdateEntities if ue.version == 1 => ue
    }

    engine.underlyingActor.updaters.size mustBe 1
  }

  test("Engine should send PoisonPill to deleted Systems") {
    val systemProbe = TestProbe()

    systemProbe.ignoreMsg({
      case _: UpdateEntities => true
      case _ => false
    })

    val props: Props = Props(classOf[ForwardingActor], systemProbe.ref)

    val sysConfigs = Set(SystemConfig(props, "forwarding-system-6"))
    val engine = TestActorRef[Engine](Props(classOf[Engine], sysConfigs, Set()))

    val f1 = system.actorSelection(engine.path / "forwarding-system-6").resolveOne
    val sysRef = Await.result(f1, 1.second)
    systemProbe.watch(sysRef)

    val probe = TestProbe()
    engine.tell(RemSystems(Set(sysRef)), probe.ref)
    probe.expectMsg(SystemsOpAck)
    systemProbe.expectTerminated(sysRef)
  }

  test("Engine should send UpdateEntities to added Systems") {
    val engine = TestActorRef[Engine](Props(classOf[Engine], Set(), Set()))

    val systemProbe = TestProbe()
    val props: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val sysConfigs = Set(SystemConfig(props, "forwarding-system-8"))

    engine ! AddSystems(sysConfigs)
    systemProbe.expectMsg(UpdateEntities(0, Set()))
  }

}
