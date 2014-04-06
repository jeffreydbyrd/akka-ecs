package game.core

import Game.Tick
import akka.actor.Actor
import akka.actor.Props
import game.entity.Entity
import akka.event.LoggingReceive
import game.systems.QuitSystem
import akka.actor.ActorRef
import game.systems.System
import game.systems.VisualSystem
import game.entity.StructureEntity
import game.components.physics.PhysicalComponent

object Stage {
  val props = Props( classOf[ Stage ] )

  // Received:
  case class Add( e: Entity )
  case class Rem( e: Entity )
}

class Stage extends Actor {
  import Game.Tick
  import Stage._

  private var entities: Set[ Entity ] = Set(
    new StructureEntity( context.actorOf( PhysicalComponent.props( 25, 1, 50, 1 ), "floor" ) ),
    new StructureEntity( context.actorOf( PhysicalComponent.props( 1, 25, 1, 50 ), "left_wall" ) ),
    new StructureEntity( context.actorOf( PhysicalComponent.props( 49, 25, 1, 50 ), "right_wall" ) ),
    new StructureEntity( context.actorOf( PhysicalComponent.props( 25, 49, 50, 1 ), "top" ) )
  )

  private var systems: Set[ ActorRef ] = Set(
    context.actorOf( QuitSystem.props( self ), "quit_system" ),
    context.actorOf( VisualSystem.props, "output_system" )
  )

  def updateEntities() = for ( sys ← systems )
    sys ! System.UpdateComponents( entities )

  override def receive = LoggingReceive {
    case Add( ent ) ⇒
      entities += ent
      updateEntities()

    case Rem( ent ) ⇒
      entities -= ent
      updateEntities()

    case Tick ⇒ for ( sys ← systems ) sys ! Tick
  }

  override def preStart() = {
    updateEntities()
  }

}