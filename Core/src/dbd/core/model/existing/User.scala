package dbd.core.model.existing

/**
  * Represents a user registered in the database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param id This user's id in DB
  * @param settings This user's current settings
  * @param languageIds Ids of the languages known by this user
  */
case class User(id: Int, settings: UserSettings, languageIds: Vector[Int])
