package dbd.api.database.model

import java.time.Instant

import dbd.core.model.existing
import dbd.core.model.partial.DescriptionData
import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.{Deprecatable, LinkedStorableFactory}

/**
  * A common trait for factories of description links
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
trait DescriptionLinkFactory[+E, +M <: Storable, -P <: DescriptionLinkLike[DescriptionData]]
	extends LinkedStorableFactory[E, existing.Description] with Deprecatable
{
	// ABSTRACT	----------------------------------
	
	/**
	  * @return Name of the attribute which contains the targeted item id
	  */
	def targetIdAttName: String
	
	/**
	  * Creates a new storable model based on specified attributes
	  * @param id Link id (optional)
	  * @param targetId Description target id (optional)
	  * @param descriptionId Description id (optional)
	  * @param deprecatedAfter Deprecation / invalidation time of this link (optional)
	  * @return A new database model with specified attributes
	  */
	def apply(id: Option[Int] = None, targetId: Option[Int] = None, descriptionId: Option[Int] = None,
			  deprecatedAfter: Option[Instant] = None): M
	
	/**
	  * Creates a new existing model based on specified data
	  * @param id Link id
	  * @param targetId Description target id
	  * @param description Description from DB
	  * @return A new existing desription link model
	  */
	protected def apply(id: Int, targetId: Int, description: existing.Description): E
	
	
	// IMPLEMENTED	------------------------------
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def childFactory = Description
	
	override def apply(model: Model[Constant], child: existing.Description) =
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			apply(valid("id").getInt, valid(targetIdAttName).getInt, child)
		}
	
	
	// COMPUTED	-----------------------------------
	
	/**
	  * @return A model that has just been marked as deprecated
	  */
	def nowDeprecated = withDeprecatedAfter(Instant.now())
	
	
	// OTHER	-----------------------------------
	
	/**
	  * @param deprecationTime Deprecation time for this description link
	  * @return A model with only deprecation time set
	  */
	def withDeprecatedAfter(deprecationTime: Instant) = apply(deprecatedAfter = Some(deprecationTime))
	
	/**
	  * Inserts a new description link to DB
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted description link
	  */
	def insert(data: P)(implicit connection: Connection) =
	{
		// Inserts the description
		val newDescription = Description.insert(data.description)
		// Inserts the link between description and device
		val linkId = apply(None, Some(data.targetId), Some(newDescription.id)).insert().getInt
		apply(linkId, data.targetId, newDescription)
	}
}