package game

import doppelengine.component.ComponentConfig
import doppelengine.core.Engine
import doppelengine.entity.EntityConfig
import doppelengine.system.SystemConfig
import game.systems.InputSystem
import akka.actor.{ActorRef, ActorSystem}
import scala.concurrent.duration._
import game.components.types.Output
import game.components.OutputComponent
import scala.concurrent.{Await, Future}

object MyGame extends App {

  implicit val timeout: akka.util.Timeout = 1.second

  private val sysConfigs: Set[SystemConfig] = Set(
    SystemConfig(InputSystem.props, "input_system")
  )

  private val output: EntityConfig =
    Map(Output -> ComponentConfig(OutputComponent.props, "output_component"))

  private val actorSystem: ActorSystem = akka.actor.ActorSystem("Doppelsystem")

  private val doppelengine: ActorRef =
    actorSystem.actorOf(Engine.props(sysConfigs, Set(output), 2.seconds), name = "engine")

  private val inputSystem = {
    val f: Future[ActorRef] =
      actorSystem.actorSelection(doppelengine.path / "input_system").resolveOne
    Await.result(f, 1000 millis)
  }

  for (ln <- io.Source.stdin.getLines()) inputSystem ! ln
}
