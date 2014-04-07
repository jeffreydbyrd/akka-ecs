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
import game.core.Game.timeout
import game.entity.Entity

object QuitSystem {
  def props( stage: ActorRef ) = Props( classOf[ QuitSystem ], stage )
}

class QuitSystem( val engine: ActorRef ) extends System {

  val requiredComponents = List( Input, Observer )
  var entities: Set[ Entity ] = Set()
  var version: Long = 0

  override def receive = LoggingReceive {
    case System.UpdateEntities( v, ents ) if v > version ⇒
      version = v
      entities =
        for ( e ← ents if e.hasComponents( requiredComponents ) )
          yield e

    // If an input says it's `quitting`, kill all its components and tell the Stage  
    case Tick ⇒
      val setOfFutures: Set[ Future[ Entity ] ] =
        entities.map { e ⇒
          ( e( Input ) ? Component.RequestSnapshot )
            .mapTo[ Snapshot ]
            .filter( _.quit )
            .map( _ ⇒ e )
        }

      val v = version // outer ref since the following executes in the future
      val futureSet: Future[ Set[ Entity ] ] = Future.sequence( setOfFutures )
      futureSet.foreach { set ⇒
        if ( set.nonEmpty ) engine ! Engine.Rem( v, set )
      }
  }
}