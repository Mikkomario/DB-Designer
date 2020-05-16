package dbd.api.database.model.description

import java.time.Instant

import dbd.api.database.factory.description.DescriptionLinkFactory
import dbd.core.model.existing.description.DescriptionLink
import dbd.core.model.partial.description.{DescriptionData, DescriptionLinkData}
import dbd.core.model.partial.description.DescriptionLinkData.PartialDescriptionLinkData
import dbd.core.model.template.DescriptionLinkLike
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Storable, Table}

/**
  * A common trait for description link model companion objects
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
trait DescriptionLinkModelFactory[+M <: Storable, -P <: DescriptionLinkLike[DescriptionData]]
{
	// ABSTRACT	----------------------------------
	
	/**
	  * @return table used by this model type
	  */
	def table: Table
	
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
	  * @return Newly inserted description link
	  */
	def insert(data: P)(implicit connection: Connection): DescriptionLink = insert(
		data.targetId, data.description)
	
	/**
	  * Inserts a new description link to DB
	  * @param targetId Id of described item
	  * @param data Description to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted description link
	  */
	def insert(targetId: Int, data: DescriptionData)(implicit connection: Connection) =
	{
		// Inserts the description
		val newDescription = DescriptionModel.insert(data)
		// Inserts the link between description and target
		val linkId = apply(None, Some(targetId), Some(newDescription.id)).insert().getInt
		DescriptionLink(linkId, DescriptionLinkData(targetId, newDescription))
	}
}

object DescriptionLinkModelFactory
{
	// OTHER	------------------------------
	
	/**
	  * @param table Targeted table
	  * @param targetIdAttName Name of the attribute that contains a link to the targeted item
	  * @return A new model factory for that type of links
	  */
	def apply(table: Table, targetIdAttName: String): DescriptionLinkModelFactory[DescriptionLinkModel[
		DescriptionLink, DescriptionLinkFactory[DescriptionLink]], PartialDescriptionLinkData] =
		DescriptionLinkModelFactoryImplementation(table, targetIdAttName)
	
	
	// NESTED	------------------------------
	
	private case class DescriptionLinkModelFactoryImplementation(table: Table, targetIdAttName: String)
		extends DescriptionLinkModelFactory[DescriptionLinkModel[DescriptionLink, DescriptionLinkFactory[DescriptionLink]],
			PartialDescriptionLinkData]
	{
		// ATTRIBUTES	----------------------
		
		private lazy val factory = DescriptionLinkFactory(this)
		
		def apply(id: Option[Int] = None, targetId: Option[Int] = None, descriptionId: Option[Int] = None,
				  deprecatedAfter: Option[Instant] = None): DescriptionLinkModel[DescriptionLink, DescriptionLinkFactory[DescriptionLink]] =
			DescriptionLinkModelImplementation(id, targetId, descriptionId, deprecatedAfter)
		
		
		// NESTED	--------------------------
		
		private case class DescriptionLinkModelImplementation(id: Option[Int] = None, targetId: Option[Int] = None,
															  descriptionId: Option[Int] = None,
															  deprecatedAfter: Option[Instant] = None)
			extends DescriptionLinkModel[DescriptionLink, DescriptionLinkFactory[DescriptionLink]]
		{
			override def factory =
				DescriptionLinkModelFactoryImplementation.this.factory
		}
	}
}