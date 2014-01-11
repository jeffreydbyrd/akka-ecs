package game.events

object AdjustHandler {
  /** Tells the target EventHandler to add 'as' to the outgoing list of Adjusts */
  case class Add( as: Set[ Adjust ] )

  /** Tells the target EventHandler to remove 'as' from the outgoing list of Adjusts */
  case class Remove( as: Set[ Adjust ] )
}

/**
 * Anything that can adjust events, such as a floor, a piece of armor, or a pair of glasses.
 * These objects can adjust events by applying one or more `Adjust` functions. They may adjust
 * both incoming and outgoing events.
 */
trait AdjustHandler {
  var outgoing: Set[ Adjust ] = Set()

  def add( adjusts: Set[ Adjust ] ) = outgoing = outgoing ++ adjusts
  def remove( adjusts: Set[ Adjust ] ) = outgoing = outgoing -- adjusts
}
