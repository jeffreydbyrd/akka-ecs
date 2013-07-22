package game

trait MobileModule {
  trait Mobile {
    val name: String
    var xPos: Int = _
    var yPos: Int = _
  }
}