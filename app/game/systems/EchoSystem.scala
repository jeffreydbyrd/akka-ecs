package game.systems

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.actorRef2Scala
import akka.pattern.{ ask, pipe }
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import game.components.ComponentType
import akka.io.TickGenerator.Tick
import game.components.InputComponent

object EchoSystem {
  def props = Props( classOf[ EchoSystem ] )
}

class EchoSystem extends Actor {
  import ComponentType.Client
  import ComponentType.Input
  import InputComponent._
  import game.core.Game.timeout

  private[ EchoSystem ] case class EchoNode(
    val inputComponent: ActorRef,
    val clientComponent: ActorRef )

  var nodes: List[ EchoNode ] = List()

  override def receive = {
    case System.UpdateComponents( entities ) ⇒
      nodes = for {
        e ← entities
        if e.components.contains( Input ) && e.components.contains( Client )
        Some( input ) = e.components.get( Input )
        Some( client ) = e.components.get( Client )
      } yield EchoNode( input, client )

    case Tick ⇒ for ( n ← nodes )
      ( n.inputComponent ? RequestSnapshot ).pipeTo( n.clientComponent )
  }
}