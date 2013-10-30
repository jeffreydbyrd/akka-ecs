package game

import java.io.Closeable
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Concurrent
import akka.actor.Actor
import scala.concurrent.duration.DurationInt
import scala.math.BigDecimal.int2bigDecimal
import scala.util.parsing.json.JSON
import scala.util.parsing.json.JSONObject
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import game.world.RoomModule
import akka.actor.ActorRef
import java.nio.BufferOverflowException

/**
 * Defines a ConnectionService data structure whose one purpose is to get data to
 * the client and to clean up resources when closed.
 */
trait ConnectionModule {

  /** Defines a Closeable service that sends String data to the client */
  trait ConnectionService extends Closeable {
    def send( data: String ): Unit
  }

  /** A simple service that uses a Play Channel object to get String data to the client */
  trait PlayConnection extends ConnectionService {
    val ( enumerator, channel ) = Concurrent.broadcast[ String ]

    override def send( d: String ) = channel push d
    override def close = channel.eofAndEnd
  }

  type MessageId = Int
  case class Ack( id: MessageId )

  /**
   * A ConnectionService that accounts for dropped messages by buffering a sent message and holding
   * it until it receives a corresponding Ack(id), at which point it removes it. If the buffer
   * grows past the `max` var, then it throws a BufferOverflowException.
   */
  trait BufferingConnection extends ConnectionService {
    var buffer: Map[ MessageId, String ] = Map.empty
    var max = 1000
    var count = 0

    def ack( id: MessageId ) { buffer = buffer - id }

    abstract override def send( d: String ) = {
      if ( buffer.size == max ) throw new BufferOverflowException()

      val id = count
      val msg = s""" {"id" : "$id", "message" : $d """
      buffer = buffer + ( id -> msg )
      count += 1
      super.send( msg )
    }
  }

  case object Close

  /** And asynchronous ConnectionService that sends every String message it receives */
  trait ActorConnection extends ConnectionService with Actor {
    override def receive = {
      case str: String ⇒ send( str )
      case Close       ⇒ close
    }
  }

  trait BufferingActorConnection extends ActorConnection with BufferingConnection {
    override def acknowledge: Receive = { case Ack( id ) ⇒ super.ack( id ) }
    override def receive = acknowledge orElse super.receive
  }

}
