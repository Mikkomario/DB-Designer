package dbd.client.model

import dbd.core.model.existing.mysql.Release

/**
  * A model that contains all displayed data concerning a release (actual or upcoming)
  * @author Mikko Hilpinen
  * @since 2.2.2020, v0.1
  */
case class DisplayedRelease(release: Option[Release], added: ChangedItems, modified: ChangedItems, removed: ChangedItems)
