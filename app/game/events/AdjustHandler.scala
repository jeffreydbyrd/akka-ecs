package game.events

object AdjustHandler {
  /** Tells the target EventHandler to add 'as' to the outgoing list of Adjusts */
  case class AddOut( as: List[ Adjust ] )

  /** Tells the target EventHandler to remove 'as' from the outgoing list of Adjusts */
  case class RemoveOut( as: List[ Adjust ] )

  /** Tells the target EventHandler to add 'as' to the incoming list of Adjusts */
  case class AddIn( as: List[ Adjust ] )

  /** Tells the target EventHandler to remove 'as' from the incoming list of Adjusts */
  case class RemoveIn( as: List[ Adjust ] )
}

/**
 * Anything that can adjust events, such as a floor, a piece of armor, or a pair of glasses.
 * These objects can adjust events by applying one or more `Adjust` functions. They may adjust
 * both incoming and outgoing events.
 */
trait AdjustHandler {
  var incoming: List[ Adjust ] = Nil
  var outgoing: List[ Adjust ] = Nil

  protected def removeAll( current: List[ Adjust ], targets: List[ Adjust ] ): List[ Adjust ] =
    current.filterNot( adj ⇒ targets.contains( adj ) )
}
