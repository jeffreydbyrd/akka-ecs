package game

package object events {

  /** A PartialFunction used by EventHandlers to handle certain Events */
  type Handle = PartialFunction[ Event, Unit ]

  /** A PartialFunction used by EventHandlers to modify incoming and outgoing Events */
  type Adjust = PartialFunction[ Event, Event ]

}