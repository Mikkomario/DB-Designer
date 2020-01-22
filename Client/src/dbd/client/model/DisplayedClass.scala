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
case class DisplayedClass(classData: Class, links: Vector[DisplayedLink] = Vector(),
						  childLinks: Vector[ChildLink] = Vector(), isExpanded: Boolean = false)
	extends ClassLike[ClassInfo, Attribute, DisplayedClass]
{
	// COMPUTED	--------------------------
	
	/**
	 * @return Id of this class
	 */
	def classId = classData.id
	
	/**
	 * @return All class ids in this class hierarchy
	 */
	def classIds: Set[Int] = Set(classId) ++ childLinks.flatMap { _.child.classIds }
	
	
	// IMPLEMENTED	----------------------
	
	override def info = classData.info
	
	override def attributes = classData.attributes
	
	override protected def makeCopy(info: ClassInfo, attributes: Vector[Attribute]) =
		copy(classData = classData.copy(info = info, attributes = attributes))
	
	override def toString = s"$classData, links: [${links.mkString(", ")}]"
	
	
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
	 * @param link A new child link
	 * @return A copy of this class with specified link added
	 */
	def withChildAdded(link: ChildLink) = copy(childLinks = childLinks :+ link)
	
	/**
	 * @param link A link to attach to this class
	 * @return A copy of this class with specified link attached (This class if the link didn't attach to this class hierarchy)
	 */
	def withLinkAdded(link: DisplayedLink) =
	{
		// May add link to this class, or a child class
		tryAddLink(link).getOrElse(this)
	}
	
	private def tryAddLink(link: DisplayedLink): Option[DisplayedClass] =
	{
		if (link.classIds.contains(classId))
			Some(copy(links = links :+ link))
		else
		{
			var mappedChild: Option[ChildLink] = None
			val mappedChildIndex = childLinks.indexWhere { l =>
				if (mappedChild.isEmpty)
				{
					mappedChild = l.child.tryAddLink(link).map { c => l.copy(child = c) }
					mappedChild.isDefined
				}
				else
					false
			}
			mappedChild.map { newChild => copy(childLinks = childLinks.updated(mappedChildIndex, newChild)) }
		}
	}
	
	/**
	 * @param link Targeted link
	 * @return a copy of this class without specified link
	 */
	def withoutLink(link: Link) =
	{
		// TODO: Take into account child links as well
		val filteredLinks = links.filterNot { _.link.id == link.id }
		if (filteredLinks.size == links.size)
			this
		else
			copy(links = filteredLinks)
	}
}
