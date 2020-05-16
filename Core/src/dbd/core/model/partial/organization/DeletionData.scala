package dbd.core.model.partial.organization

import java.time.Instant

/**
  * Contains basic information about an organization deletion attempt
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  * @param organizationId Id of the targeted organization
  * @param creatorId Id of the user who attempted deletion
  * @param actualizationTime Time when this deletion actualizes if not cancelled
  */
case class DeletionData(organizationId: Int, creatorId: Int, actualizationTime: Instant)