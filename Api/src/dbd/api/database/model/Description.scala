package dbd.api.database.model

import dbd.api.database.Tables
import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.existing
import dbd.core.model.partial.DescriptionData
import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactory

object Description extends StorableFactory[existing.Description]
{
	// IMPLEMENTED	--------------------------------
	
	override def apply(model: template.Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		DescriptionRole.forId(valid("roleId").getInt).map { role =>
			existing.Description(valid("id").getInt, DescriptionData(role, valid("languageId").getInt,
				valid("text").getString, valid("authorId").int))
		}
	}
	
	override def table = Tables.description
	
	
	// OTHER	-------------------------------------
	
	/**
	  * Inserts a new description to the DB
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted description
	  */
	def insert(data: DescriptionData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.role), Some(data.languageId), Some(data.text), data.authorId).insert().getInt
		existing.Description(newId, data)
	}
}

/**
  * Used for interacting with descriptions in DB
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class Description(id: Option[Int] = None, role: Option[DescriptionRole] = None, languageId: Option[Int] = None,
					   text: Option[String] = None, authorId: Option[Int] = None)
	extends StorableWithFactory[existing.Description]
{
	override def factory = Description
	
	override def valueProperties = Vector("id" -> id, "roleId" -> role.map { _.id }, "languageId" -> languageId,
		"text" -> text, "authorId" -> authorId)
}
