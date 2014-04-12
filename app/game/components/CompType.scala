package game.components

import game.components.io.InputComp

trait CompType[ T ]

object CompType {
  case object Input extends CompType[ InputComp ]
}