package game.systems

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive
import akka.pattern.ask
import game.components.Component
import game.components.ComponentType.Input
import game.components.ComponentType.Observer
import game.components.io.InputComponent.Snapshot
import game.core.Engine
import game.core.Engine.Tick
import game.core.Engine.TickAck
import game.core.Engine.timeout
import game.entity.Entity

object QuitSystem {
  def props( stage: ActorRef ) = Props( classOf[ QuitSystem ], stage )
}

class QuitSystem( val engine: ActorRef ) extends System {

  val requiredComponents = List( Input, Observer )

  override def receive = manage( 0, Set() )

  def manage( version: Long, entities: Set[ Entity ] ): Receive =
    LoggingReceive {
      case System.UpdateEntities( v, ents ) if v > version ⇒
        val es = for ( e ← ents if e.hasComponents( requiredComponents ) )
          yield e
        context.become( manage( v, es ) )

      case Tick ⇒
        val setOfFutures: Set[ Future[ Entity ] ] =
          entities.map { e ⇒
            ( e( Input ) ? Component.RequestSnapshot )
              .mapTo[ Snapshot ]
              .filter( _.quit )
              .map( _ ⇒ e )
          }

        val futureSet: Future[ Set[ Entity ] ] = Future.sequence( setOfFutures )
        futureSet.foreach { set ⇒
          if ( set.nonEmpty ) engine ! Engine.Rem( version, set )
        }
        
        sender ! TickAck
    }
}