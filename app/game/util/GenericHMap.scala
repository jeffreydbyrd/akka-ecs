package game.util

object GenericHMap {
  def empty[ K[ _ ], V[ _ ] ] = new GenericHMapImpl()
}

trait GenericHMap[ K[ _ ], V[ _ ] ] {
  val underlying: Map[ Any, Any ]
  def apply[ A, C <: K[ A ] ]( k: C ): V[ A ] =
    underlying( k ).asInstanceOf[ V[ A ] ]

  def get[ A, C <: K[ A ] ]( k: C ): Option[ V[ A ] ] =
    underlying.get( k ).asInstanceOf[ Option[ V[ A ] ] ]
}

sealed class GenericHMapImpl[ K[ _ ], V[ _ ] ]( val underlying: Map[ Any, Any ] = Map.empty ) extends GenericHMap[ K, V ] {
  def +[ A, C <: K[ A ] ]( kv: ( C, V[ A ] ) ): GenericHMapImpl[ K, V ] =
    new GenericHMapImpl( underlying + kv )

  def -[ A, C <: K[ A ] ]( k: C ): GenericHMapImpl[ K, V ] =
    new GenericHMapImpl( underlying - k )
}