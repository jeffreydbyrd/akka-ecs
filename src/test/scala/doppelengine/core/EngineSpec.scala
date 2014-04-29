package doppelengine.core

import akka.testkit.{TestProbe, TestKit}
import akka.actor._
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import scala.concurrent.duration._
import doppelengine.system.SystemConfig
import akka.util.Timeout
import doppelengine.system.System.UpdateEntities
import doppelengine.core.Engine.{Rem, Tick, OpSuccess, Add}
import doppelengine.component.ComponentConfig
import doppelengine.entity.{Entity, EntityConfig}
import scala.concurrent.Await

class EngineSpec
  extends TestKit(ActorSystem("EngineSpec"))
  with FunSuiteLike
  with BeforeAndAfterAll {

  implicit val timeout: Timeout = 1.second

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("1: Engine should update Systems on instantiation") {
    val probe = TestProbe()
    val props: Props = Props(classOf[ForwardingActor], probe.ref)
    val sysConfigs = Set(SystemConfig(props, "forwarding-system-1"))
    system.actorOf(Props(classOf[Engine], sysConfigs, Set(), 1000 millis))
    probe.expectMsg(UpdateEntities(0, Set()))
  }

  test("2: Engine should update Systems when I Add a component") {
    val systemProbe = TestProbe()
    val componentProbe = TestProbe()
    systemProbe.ignoreMsg {
      case UpdateEntities(_, ents) if ents.size == 0 => true
      case Tick => true
      case _ => false
    }

    val sysProps: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val comProps: Props = Props(classOf[ForwardingActor], componentProbe.ref)

    val sysConfigs = Set(SystemConfig(sysProps, "forwarding-system-2"))
    val engine = system.actorOf(Props(classOf[Engine], sysConfigs, Set(), 1000 millis))

    val compConfig = ComponentConfig(comProps, "forwarding-component-2")
    val entityConfig: EntityConfig = Map(TestComponent -> compConfig)

    val probe = TestProbe()
    probe.send(engine, Add(0, Set(entityConfig)))
    probe.expectMsg(OpSuccess(1))

    systemProbe.expectMsgPF() {
      case ue@UpdateEntities(1, ents) if ents.size > 0 => ue
    }
  }

  test("3: Engine should terminate components when Removed") {
    val systemProbe = TestProbe()
    val componentProbe = TestProbe()
    systemProbe.ignoreMsg {
      case Tick => true
      case _ => false
    }

    val sysProps: Props = Props(classOf[ForwardingActor], systemProbe.ref)
    val comProps: Props = Props(classOf[ForwardingActor], componentProbe.ref)

    val sysConfigs = Set(SystemConfig(sysProps, "forwarding-system-3"))
    val compConfig = ComponentConfig(comProps, "forwarding-component-3")
    val entityConfigs = Set(Map(TestComponent -> compConfig))
    val engine = system.actorOf(Props(classOf[Engine], sysConfigs, entityConfigs, 1000 millis))

    val f = system.actorSelection(engine.path / "forwarding-component-3").resolveOne
    val compRef = Await.result(f, 1.second)
    componentProbe.watch(compRef)

    val ents: Set[Entity] =
      systemProbe.expectMsgPF() {
        case ue@UpdateEntities(0, ents) if ents.size == 1 => ents
      }

    val probe = TestProbe()
    probe.send(engine, Rem(0, ents))
    probe.expectMsg(OpSuccess(1))

    componentProbe.expectTerminated(compRef)
  }

}
