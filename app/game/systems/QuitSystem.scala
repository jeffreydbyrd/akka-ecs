package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.actorRef2Scala
import akka.pattern.{ ask, pipe }
import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.LoggingReceive
import game.components.ComponentType
import game.components.InputComponent
import akka.actor.Props
import akka.actor.PoisonPill
import game.entity.EntityId
import game.entity.Entity
import game.core.Stage
import game.entity.PlayerEntity

object QuitSystem {
  def props( stage: ActorRef ) = Props( classOf[ QuitSystem ], stage )
}

class QuitSystem( val stage: ActorRef ) extends System {
  import game.core.Game.Tick
  import game.core.Game.timeout
  import ComponentType._
  import InputComponent._

  private[ QuitSystem ] case class QuitNode(
    val ent: Entity,
    val inputComponent: ActorRef,
    val clientComponent: ActorRef )

  var nodes: Set[ QuitNode ] = Set()

  override def receive = LoggingReceive {
    case System.UpdateComponents( entities ) ⇒
      // Find all Entities with an InputComponent and ClientComponent 
      nodes = for {
        e ← entities
        if e.components.contains( Input ) && e.components.contains( Client )
        Some( input ) = e.components.get( Input )
        Some( client ) = e.components.get( Client )
      } yield QuitNode( e, input, client )

    // If an input says it's `quitting`, kill all its components and tell the Stage  
    case Tick ⇒ for ( n ← nodes )
      ( n.inputComponent ? RequestSnapshot ) foreach {
        case snap @ Snapshot( _, _, _, quitting ) ⇒
          println( snap )
          if ( quitting ) {
            stage ! Stage.Rem( n.ent )
            for ( ( _, comp ) ← n.ent.components ) comp ! PoisonPill
          }
      }
  }
}