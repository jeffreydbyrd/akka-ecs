package engine.util.logging

/**
 * Defines basic logging behavior with a LoggingService that must be implemented. Since we aim to
 * keep the Play Framework decoupled from the game logic, we don't want to commit to one logging
 * tool over another. Therefore, throughout the app, we will use our own logging service whose
 * behavior is implemented using whichever tool the coder wants. Use the Play serice when writing
 * Controllers and other Play-specific code, and use the Akka service when inside an EventHandler.
 */
trait LoggingService {
  def info( s: String ): Unit
  def debug( s: String ): Unit
  def warn( s: String ): Unit
  def error( s: String ): Unit
}

class PlayLoggingService extends LoggingService {
  import play.Logger

  def info( s: String ) = Logger.info( s )
  def debug( s: String ) = Logger.debug( s )
  def warn( s: String ) = Logger.warn( s )
  def error( s: String ) = Logger.error( s )
}

class AkkaLoggingService( actor: akka.actor.Actor, context: akka.actor.ActorContext ) extends LoggingService {
  import akka.event.Logging
  val log = Logging( context.system, actor )

  def info( s: String ) = log.info( s )
  def debug( s: String ) = log.debug( s )
  def warn( s: String ) = log.warning( s )
  def error( s: String ) = log.error( s )
}