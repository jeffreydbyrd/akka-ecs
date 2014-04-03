package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.actorRef2Scala
import akka.pattern.{ ask, pipe }
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import game.components.ComponentType
import game.components.InputComponent
import akka.event.LoggingReceive
import game.core.Game.timeout

object EchoSystem {
  val props = Props( classOf[ EchoSystem ] )
}

class EchoSystem extends Actor {
  import game.core.Game.Tick
  import ComponentType._
  import InputComponent._

  private[ EchoSystem ] case class EchoNode(
    val inputComponent: ActorRef,
    val clientComponent: ActorRef )

  var nodes: Set[ EchoNode ] = Set()

  override def receive = LoggingReceive {
    case System.UpdateComponents( entities ) ⇒
      // Find all Entities with an InputComponent and ClientComponent 
      nodes = for {
        e ← entities
        if e.components.contains( Input ) && e.components.contains( Client )
        Some( input ) = e.components.get( Input )
        Some( client ) = e.components.get( Client )
      } yield EchoNode( input, client )

    case Tick ⇒
      for ( n ← nodes )
        ( n.inputComponent ? RequestSnapshot ).pipeTo( n.clientComponent )
  }
}