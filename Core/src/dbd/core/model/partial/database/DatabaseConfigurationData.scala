package dbd.core.model.partial.database

/**
  * Contains basic data about a single database configuration
  * @author Mikko Hilpinen
  * @since 23.5.2020, v2
  */
case class DatabaseConfigurationData(name: String, creatorId: Option[Int] = None)
