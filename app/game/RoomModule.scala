package game

trait RoomModule extends EventModule {

  trait EHRoom
      extends GenericRoom
      with EventHandler {
    
    
    
  }

  trait GenericRoom {
    val id: String
  }

}