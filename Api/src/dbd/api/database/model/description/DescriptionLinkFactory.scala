package dbd.api.database.model.description

import java.time.Instant

import dbd.core.model.existing
import dbd.core.model.partial.DescriptionData
import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.{Deprecatable, LinkedStorableFactory}

import scala.util.Try

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
	protected def apply(id: Int, targetId: Int, description: existing.Description): Try[E]
	
	
	// IMPLEMENTED	------------------------------
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def childFactory = Description
	
	override def apply(model: Model[Constant], child: existing.Description) =
		table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
			apply(valid("id").getInt, valid(targetIdAttName).getInt, child)
		}
	
	
	// COMPUTED	-----------------------------------
	
	/**
	  * @return Column that refers to described items/targets
	  */
	def targetIdColumn = table(targetIdAttName)
	
	/**
	  * @return A model that has just been marked as deprecated
	  */
	def nowDeprecated = withDeprecatedAfter(Instant.now())
	
	
	// OTHER	-----------------------------------
	
	/**
	  * @param targetId Id of description target
	  * @return A model with only target id set
	  */
	def withTargetId(targetId: Int) = apply(targetId = Some(targetId))
	
	/**
	  * @param deprecationTime Deprecation time for this description link
	  * @return A model with only deprecation time set
	  */
	def withDeprecatedAfter(deprecationTime: Instant) = apply(deprecatedAfter = Some(deprecationTime))
	
	/**
	  * Inserts a new description link to DB
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted description link's id + newly inserted description model
	  */
	def insert(data: P)(implicit connection: Connection): (Int, existing.Description) = insert(
		data.targetId, data.description)
	
	/**
	  * Inserts a new description link to DB
	  * @param targetId Id of described item
	  * @param data Description to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted description link's id + newly inserted description model
	  */
	def insert(targetId: Int, data: DescriptionData)(implicit connection: Connection) =
	{
		// Inserts the description
		val newDescription = Description.insert(data)
		// Inserts the link between description and target
		val linkId = apply(None, Some(targetId), Some(newDescription.id)).insert().getInt
		linkId -> newDescription
	}
}
