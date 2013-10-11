package game.util.logging

trait LoggingModule {
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
}