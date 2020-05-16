package dbd.core.model.existing.description

import dbd.core.model.partial.description.DescriptionLinkData.FullDescriptionLinkData

/**
  * Represents a stored description link of some type
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DescriptionLink(id: Int, data: FullDescriptionLinkData) extends StoredDescriptionLink[FullDescriptionLinkData]
