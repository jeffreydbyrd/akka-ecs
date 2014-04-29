package doppelengine.core

import akka.actor.{PoisonPill, Actor, ActorRef}
import akka.event.LoggingReceive

/**
 * A simple actor that forwards all messages to a receiver
 */
class ForwardingActor(receiver: ActorRef) extends Actor {
  override def receive: Receive = LoggingReceive {
    case msg => receiver forward msg
  }
}
