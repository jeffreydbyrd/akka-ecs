package models

import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import models.Commands._
import akka.actor.PoisonPill
import akka.actor.ActorRef

trait PlayerModule {

  object Player {
    def apply( username: String ) = Akka.system.actorOf( Props( new ConnectedPlayer( username ) ) )
  }

  class ConnectedPlayer( val username: String ) extends Player with Actor {
    val status = None // will change when we implement connection code

    override def receive = {
      case Start( con: ActorRef ) ⇒
        sender ! status
        connection = con
        context become playing
    }

    def playing: Receive = {
      case Command( json ) ⇒
    }
  }

  trait Player {
    val status: Option[ String ]
    var connection:ActorRef = _
  }
}