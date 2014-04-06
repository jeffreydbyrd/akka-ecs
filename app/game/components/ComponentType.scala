package game.components

trait ComponentType

object ComponentType {
  case object Input extends ComponentType
  case object Observer extends ComponentType
  case object Physical extends ComponentType
}