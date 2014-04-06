package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.actorRef2Scala
import akka.pattern.{ ask, pipe }
import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.LoggingReceive
import game.components.ComponentType
import game.components.io.InputComponent
import akka.actor.Props
import akka.actor.PoisonPill
import game.entity.EntityId
import game.entity.Entity
import game.core.Stage
import game.entity.PlayerEntity
import game.components.Component

object QuitSystem {
  def props( stage: ActorRef ) = Props( classOf[ QuitSystem ], stage )
}

class QuitSystem( val stage: ActorRef ) extends System {
  import game.core.Game.Tick
  import game.core.Game.timeout
  import InputComponent._

  protected case class QuitNode(
    val ent: Entity,
    val inputComponent: ActorRef,
    val clientComponent: ActorRef )

  var nodes: Set[ QuitNode ] = Set()
  val requiredComponents = List(
    ComponentType.Input, // reads from  
    ComponentType.Observer // writes to
  )

  override def receive = LoggingReceive {
    case System.UpdateComponents( entities ) ⇒
      // Find all Entities with an InputComponent and ClientComponent 
      nodes = for {
        e ← entities if e.hasComponents( requiredComponents )
        input = e.components( ComponentType.Input )
        output = e.components( ComponentType.Observer )
      } yield QuitNode( e, input, output )

    // If an input says it's `quitting`, kill all its components and tell the Stage  
    case Tick ⇒ for ( n ← nodes )
      ( n.inputComponent ? Component.RequestSnapshot ) foreach {
        case snap @ Snapshot( _, _, _, quitting ) ⇒
          if ( quitting ) {
            stage ! Stage.Rem( n.ent )
            for ( ( _, comp ) ← n.ent.components ) comp ! PoisonPill
          }
      }
  }
}