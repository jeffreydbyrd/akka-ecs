package game.core

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import game.components.physics.PositionComponent
import game.entity.Entity
import game.entity.StructureEntity
import game.systems.QuitSystem
import game.systems.System
import game.systems.VisualSystem

object Engine {
  val props = Props( classOf[ Engine ] )

  // Received:
  case class NewPlayer( entity: Entity )
  trait EntityOp {
    val v: Long
    val es: Set[ Entity ]
  }
  case class Add( val v: Long, val es: Set[ Entity ] ) extends EntityOp
  case class Rem( val v: Long, val es: Set[ Entity ] ) extends EntityOp
  case object Tick
}

class Engine extends Actor {
  import Engine._

  val ticker =
    Game.system.scheduler.schedule( 5000 milliseconds, 5000 milliseconds, self, Tick )

  private var systems: Set[ ActorRef ] = Set(
    context.actorOf( QuitSystem.props( self ), "quit_system" ),
    context.actorOf( VisualSystem.props, "visual_system" )
  )

  def updateEntities( v: Long, ents: Set[ Entity ] ) = for ( sys ← systems ) {
    sys ! System.UpdateEntities( v, ents )
    context.become( manage( v, ents ) )
  }

  override def receive = manage( 0, Set() )

  def manage( version: Long, entities: Set[ Entity ] ): Receive = LoggingReceive {
    case NewPlayer( ent )     ⇒ self ! Add( version, Set( ent ) )
    case Tick                 ⇒ for ( sys ← systems ) sys ! Tick
    case Add( `version`, es ) ⇒ updateEntities( version + 1, entities ++ es )
    case Rem( `version`, es ) ⇒
      updateEntities( version + 1, entities -- es )
      for ( e ← es; ( _, comp ) ← e.components ) comp ! PoisonPill
    case op: EntityOp if op.v < version ⇒
      sender ! System.UpdateEntities( version, entities )
  }

  override def preStart() = {
    var walls: Set[ Entity ] = Set(
      new StructureEntity( context.actorOf( PositionComponent.props( 25, 1, 50, 1 ), "floor" ) )
    //    new StructureEntity( context.actorOf( PositionComponent.props( 1, 25, 1, 50 ), "left_wall" ) ),
    //    new StructureEntity( context.actorOf( PositionComponent.props( 49, 25, 1, 50 ), "right_wall" ) ),
    //    new StructureEntity( context.actorOf( PositionComponent.props( 25, 49, 50, 1 ), "top" ) )
    )

    self ! Add( 0, walls )
  }

}