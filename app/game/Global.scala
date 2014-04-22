package game

import engine.core.Game

/**
 * Required by the PlayController. User must provide value for `game`
 */
object Global {
  val game: Game = new MyGame
}
