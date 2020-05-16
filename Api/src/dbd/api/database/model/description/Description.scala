package dbd.api.database.model.description

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
	// ATTRIBUTES	--------------------------------
	
	/**
	  * Name of the attribute that contains description role id
	  */
	val descriptionRoleIdAttName = "roleId"
	
	
	// IMPLEMENTED	--------------------------------
	
	override def apply(model: template.Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		DescriptionRole.forId(valid(descriptionRoleIdAttName).getInt).map { role =>
			existing.Description(valid("id").getInt, DescriptionData(role, valid("languageId").getInt,
				valid("text").getString, valid("authorId").int))
		}
	}
	
	override def table = Tables.description
	
	
	// COMPUTED	-------------------------------------
	
	/**
	  * @return Column that contains description role id
	  */
	def descriptionRoleIdColumn = table(descriptionRoleIdAttName)
	
	
	// OTHER	-------------------------------------
	
	/**
	  * @param role Description role
	  * @return A model with only description role set
	  */
	def withRole(role: DescriptionRole) = apply(role = Some(role))
	
	/**
	  * @param languageId Description language id
	  * @return A model with only language id set
	  */
	def withLanguageId(languageId: Int) = apply(languageId = Some(languageId))
	
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
	import Description._
	
	// IMPLEMENTED	--------------------------------
	
	override def factory = Description
	
	override def valueProperties = Vector("id" -> id, descriptionRoleIdAttName -> role.map { _.id },
		"languageId" -> languageId, "text" -> text, "authorId" -> authorId)
	
	
	// OTHER	------------------------------------
	
	/**
	  * @param languageId Id of description language
	  * @return A copy of this model with specified language
	  */
	def withLanguageId(languageId: Int) = copy(languageId = Some(languageId))
}
