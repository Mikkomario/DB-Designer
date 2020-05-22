package dbd.api.database.access.many.description

import dbd.api.database.factory.description.DescriptionLinkFactory
import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.existing.description.Description
import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess
import utopia.vault.sql.Extensions._

/**
  * A common trait for description link access points
  * @author Mikko Hilpinen
  * @since 17.5.2020, v2
  */
trait DescriptionLinksAccess[+A <: DescriptionLinkLike[Description]] extends ManyModelAccess[A]
{
	// ABSTRACT	-------------------------
	
	override def factory: DescriptionLinkFactory[A]
	
	
	// COMPUTED	------------------------
	
	/**
	  * @return A model factory used for constructing description search models
	  */
	protected def descriptionFactory = factory.childFactory
	
	
	// OTHER	------------------------
	
	/**
	  * @param languageId Id of targeted language
	  * @return An access point to a subset of these descriptions. Only contains desriptions written in that language.
	  */
	def inLanguageWithId(languageId: Int) = DescriptionsInLanguage(languageId)
	
	
	// NESTED	-------------------------
	
	case class DescriptionsInLanguage(languageId: Int) extends ManyModelAccess[A]
	{
		// IMPLEMENTED	-----------------
		
		override def factory = DescriptionLinksAccess.this.factory
		
		override val globalCondition = Some(DescriptionLinksAccess.this.mergeCondition(
			descriptionFactory.withLanguageId(languageId).toCondition))
		
		
		// OTHER	---------------------
		
		/**
		  * @param role Targeted description role
		  * @param connection Db Connection
		  * @return Description for that role for this item in targeted language
		  */
		def apply(role: DescriptionRole)(implicit connection: Connection): Option[A] =
			apply(Set(role)).headOption
		
		/**
		  * @param roles Targeted description roles
		  * @param connection DB Connection (implicit)
		  * @return Recorded descriptions for those roles (in this language & target)
		  */
		def apply(roles: Set[DescriptionRole])(implicit connection: Connection) =
		{
			if (roles.nonEmpty)
				read(Some(descriptionFactory.descriptionRoleIdColumn.in(roles.map { _.id })))
			else
				Vector()
		}
		
		/**
		  * Reads descriptions of this item, except those in excluded description roles
		  * @param excludedRoles Excluded description roles
		  * @param connection DB Connection (implicit)
		  * @return Read description links
		  */
		def forRolesOutside(excludedRoles: Set[DescriptionRole])(implicit connection: Connection) =
			apply(DescriptionRole.values.toSet -- excludedRoles)
	}
}
