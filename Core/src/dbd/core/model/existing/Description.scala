package dbd.core.model.existing

import dbd.core.model.partial.DescriptionData

/**
  * Represents a description stored in the database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class Description(id: Int, data: DescriptionData) extends Stored[DescriptionData]
