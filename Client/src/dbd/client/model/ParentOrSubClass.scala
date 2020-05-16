package dbd.client.model

import dbd.core.model.existing.database
import dbd.core.model.existing.database.{Attribute, ClassInfo}
import dbd.core.model.template.ClassLike
import utopia.flow.util.CollectionExtensions._

object ParentOrSubClass
{
	/**
	 * @param classToDisplay A top level class
	 * @return Wrapped class
	 */
	def topLevel(classToDisplay: DisplayedClass) = ParentOrSubClass(Left(classToDisplay))
	
	/**
	 * @param parent The parent class
	 * @param link A link from the parent class to a sub-class
	 * @return Wrapped sub class
	 */
	def subClass(parent: database.Class, link: ChildLink) = ParentOrSubClass(Right(parent, link))
	
	/**
	 * @param parent The parent class
	 * @return Wrapped subclasses of the specified parent class
	 */
	def subClasses(parent: DisplayedClass) = parent.childLinks.map { l =>
		subClass(parent.classData, l) }
}

/**
 * Contains data for either a top level class or a sub-class under a parent
 * @author Mikko Hilpinen
 * @since 26.1.2020, v0.1
 */
case class ParentOrSubClass(data: Either[DisplayedClass, (database.Class, ChildLink)])
	extends ClassLike[ClassInfo, Attribute, ParentOrSubClass]
{
	// COMPUTED	------------------------
	
	/**
	 * @return The class that is displayed on this configuration
	 */
	def displayedClass = data match
	{
		case Right(subLevel) => subLevel._2.child
		case Left(topLevel) => topLevel
	}
	
	/**
	 * @return Whether this class is currently in an expanded state
	 */
	def isExpanded = displayedClass.isExpanded
	
	/**
	 * @return The sub-classes of displayed class
	 */
	def subClasses = ParentOrSubClass.subClasses(displayedClass)
	
	/**
	  * @return Id of this class
	  */
	def classId = data.mapToSingle { _.classId } { _._1.id }
	
	
	// IMPLEMENTED	--------------------
	
	override def info = displayedClass.info
	
	override def attributes = displayedClass.attributes
	
	override def makeCopy(info: ClassInfo, attributes: Vector[Attribute]) = copy(data = data match
	{
		case Right(subLevel) => Right(subLevel._1 -> subLevel._2.mapClass { _.makeCopy(info, attributes) })
		case Left(topLevel) => Left(topLevel.makeCopy(info, attributes))
	})
}
