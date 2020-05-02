package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.partial.{DescriptionData, DeviceDescriptionData}
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, LinkedStorableFactory}

object DeviceDescription extends LinkedStorableFactory[existing.DeviceDescription, existing.Description] with Deprecatable
{
	// IMPLEMENTED	------------------------------
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def childFactory = Description
	
	override def apply(model: Model[Constant], child: existing.Description) =
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			existing.DeviceDescription(valid("id").getInt, DeviceDescriptionData(valid("deviceId").getInt, child))
		}
	
	override def table = Tables.deviceDescription
	
	
	// OTHER	-----------------------------------
	
	/**
	  * Inserts a new device description to DB
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted description
	  */
	def insert(data: DeviceDescriptionData[DescriptionData])(implicit connection: Connection) =
	{
		// Inserts the description
		val newDescription = Description.insert(data.description)
		// Inserts the link between description and device
		val linkId = apply(None, Some(data.deviceId), Some(newDescription.id)).insert().getInt
		existing.DeviceDescription(linkId, data.copy(description = newDescription))
	}
}

/**
  * Used for interacting with links between devices and their descriptions
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class DeviceDescription(id: Option[Int] = None, deviceId: Option[Int] = None, descriptionId: Option[Int] = None,
							 deprecatedAfter: Option[Instant] = None) extends StorableWithFactory[existing.DeviceDescription]
{
	override def factory = DeviceDescription
	
	override def valueProperties = Vector("id" -> id, "deviceId" -> deviceId, "descriptionId" -> descriptionId,
		"deprecatedAfter" -> deprecatedAfter)
}
