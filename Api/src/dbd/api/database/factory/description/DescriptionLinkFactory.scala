package dbd.api.database.factory.description

import dbd.api.database.model.description.{DescriptionLinkModel, DescriptionLinkModelFactory, DescriptionModel}
import dbd.core.model.existing
import dbd.core.model.existing.description.{Description, DescriptionLink}
import dbd.core.model.partial.description.DescriptionLinkData
import dbd.core.model.partial.description.DescriptionLinkData.PartialDescriptionLinkData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.{Storable, Table}
import utopia.vault.nosql.factory.{Deprecatable, LinkedFactory}

import scala.util.{Success, Try}

/**
  * A common trait for factories of description links
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
trait DescriptionLinkFactory[+E] extends LinkedFactory[E, existing.description.Description] with Deprecatable
{
	// ABSTRACT	----------------------------------
	
	/**
	  * @return Factory used in database model construction
	  */
	def modelFactory: DescriptionLinkModelFactory[_, _]
	
	/**
	  * Creates a new existing model based on specified data
	  * @param id Link id
	  * @param targetId Description target id
	  * @param description Description from DB
	  * @return A new existing desription link model
	  */
	protected def apply(id: Int, targetId: Int, description: existing.description.Description): Try[E]
	
	
	// IMPLEMENTED	------------------------------
	
	override def table = modelFactory.table
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def childFactory = DescriptionModel
	
	override def apply(model: Model[Constant], child: existing.description.Description) =
		table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
			apply(valid("id").getInt, valid(modelFactory.targetIdAttName).getInt, child)
		}
}

object DescriptionLinkFactory
{
	// ATTRIBUTES	------------------------------
	
	/**
	  * Description role description factory
	  */
	val descriptionRole = apply(DescriptionLinkModel.descriptionRole)
	
	/**
	  * Device description factory
	  */
	val device = apply(DescriptionLinkModel.device)
	
	/**
	  * Organization description factory
	  */
	val organization = apply(DescriptionLinkModel.organization)
	
	/**
	  * Role description factory
	  */
	val role = apply(DescriptionLinkModel.role)
	
	/**
	  * Task description factory
	  */
	val task = apply(DescriptionLinkModel.task)
	
	/**
	  * Language description factory
	  */
	val language = apply(DescriptionLinkModel.language)
	
	
	// OTHER	----------------------------------
	
	/**
	  * @param table Targeted table
	  * @param targetIdAttName Name of the attribute that contains the link to the described item
	  * @return A new factory for reading description links
	  */
	def apply(table: Table, targetIdAttName: String): DescriptionLinkFactory[DescriptionLink] =
		apply(DescriptionLinkModelFactory(table, targetIdAttName))
	
	/**
	  * Wraps a description link model factory into a description link factory
	  * @param modelFactory model to wrap
	  * @return A new link factory
	  */
	def apply(modelFactory: DescriptionLinkModelFactory[Storable, PartialDescriptionLinkData]): DescriptionLinkFactory[DescriptionLink] =
		DescriptionLinkFactoryImplementation(modelFactory)
	
	
	// NESTED	----------------------------------
	
	private case class DescriptionLinkFactoryImplementation(modelFactory: DescriptionLinkModelFactory[Storable, PartialDescriptionLinkData])
		extends DescriptionLinkFactory[DescriptionLink]
	{
		override protected def apply(id: Int, targetId: Int, description: Description) =
			Success(DescriptionLink(id, DescriptionLinkData(targetId, description)))
	}
}
