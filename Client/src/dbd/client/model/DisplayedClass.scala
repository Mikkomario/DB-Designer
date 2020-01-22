package dbd.client.model

import dbd.core.model.existing.{Attribute, Class, ClassInfo, Link}
import dbd.core.model.template.ClassLike

/**
 * Contains necessary data when displaying a class in the UI
 * @author Mikko Hilpinen
 * @since 20.1.2020, v0.1
 * @param classData Class to display
 * @param links Links from this class to other classes or from other classes to this one (paired with the other class)
 * @param isExpanded Whether the class display should be expanded (default = false)
 */
case class DisplayedClass(classData: Class, links: Vector[DisplayedLink] = Vector(), isExpanded: Boolean = false)
	extends ClassLike[ClassInfo, Attribute, DisplayedClass]
{
	// COMPUTED	--------------------------
	
	/**
	 * @return Id of this class
	 */
	def classId = classData.id
	
	
	// IMPLEMENTED	----------------------
	
	override def info = classData.info
	
	override def attributes = classData.attributes
	
	override protected def makeCopy(info: ClassInfo, attributes: Vector[Attribute]) =
		copy(classData = classData.copy(info = info, attributes = attributes))
	
	
	// OTHER	--------------------------
	
	/**
	 * @param expanded Whether the class display should be expanded
	 * @return A copy of this model with specified expand state
	 */
	def withExpandState(expanded: Boolean) = copy(isExpanded = expanded)
	
	/**
	 * @param newClass New class state to display
	 * @return A copy of this display with specified class
	 */
	def withClass(newClass: Class) = copy(classData = newClass)
	
	/**
	 * @param classId Id of another class
	 * @return A copy of this class display without any links to specified class
	 */
	def withoutLinksToClassWithId(classId: Int) = copy(links = links.filterNot { _.otherClass.id == classId })
	
	/**
	 * @param link A link to attach to this class
	 * @return A copy of this class with specified link attached
	 */
	def withLinkAdded(link: DisplayedLink) = copy(links = links :+ link)
}
