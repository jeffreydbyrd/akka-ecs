package engine.component

trait ComponentType

object ComponentType {
  case object Input extends ComponentType
  case object Observer extends ComponentType
  case object Dimension extends ComponentType
  case object Mobility extends ComponentType
}