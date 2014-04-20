package engine.util.hmap

object GenericHMap {
  def empty[ K[ _ ], V[ _ ] ] = new GenericHMapImpl[ K, V ]()
}

trait GenericHMap[ K[ _ ], V[ _ ] ] {
  val underlying: Map[ Any, Any ]

  def apply[ A ]( k: K[ A ] ): V[ A ] =
    underlying( k ).asInstanceOf[ V[ A ] ]

  def get[ A ]( k: K[ A ] ): Option[ V[ A ] ] =
    underlying.get( k ).asInstanceOf[ Option[ V[ A ] ] ]

  def +[ A ]( kv: ( K[ A ], V[ A ] ) ): GenericHMap[ K, V ]
  def -[ A ]( k: K[ A ] ): GenericHMap[ K, V ]
}

sealed class GenericHMapImpl[ K[ _ ], V[ _ ] ]( val underlying: Map[ Any, Any ] = Map.empty )
    extends GenericHMap[ K, V ] {

  override def +[ A ]( kv: ( K[ A ], V[ A ] ) ): GenericHMap[ K, V ] =
    new GenericHMapImpl( underlying + kv )

  override def -[ A ]( k: K[ A ] ): GenericHMap[ K, V ] =
    new GenericHMapImpl( underlying - k )
}