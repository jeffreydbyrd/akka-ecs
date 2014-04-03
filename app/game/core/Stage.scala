package game.core

import Game.Tick
import akka.actor.Actor
import akka.actor.Props
import game.entity.Entity
import akka.event.LoggingReceive
import game.systems.EchoSystem
import akka.actor.ActorRef
import game.systems.QuitSystem

object Stage {
  val props = Props( classOf[ Stage ] )

  // Received:
  case class Add( e: Entity )
  case class Rem( e: Entity )
}

class Stage extends Actor {
  import Game.Tick
  import Stage._

  private var entities: Set[ Entity ] = Set()
  private var systems: Set[ ActorRef ] = Set(
    context.actorOf( QuitSystem.props, "quit_system" )
  )

  def updateEntities() =
    for ( sys ← systems )
      sys ! game.systems.System.UpdateComponents( entities )

  override def receive = LoggingReceive {
    case Add( ent ) ⇒
      entities += ent
      updateEntities()

    case Rem( ent ) ⇒
      entities -= ent
      updateEntities()

    case Tick ⇒ for ( sys ← systems ) sys ! Tick
  }

}