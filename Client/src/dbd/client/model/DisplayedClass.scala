package dbd.client.model

import dbd.core.model.existing.{Attribute, Class, ClassInfo, Link}
import dbd.core.model.template.ClassLike
import utopia.flow.util.CollectionExtensions._

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
	
	/**
	 * @return All child links in this class hierarchy
	 */
	def allChildLinks: Vector[ChildLink] = childLinks ++ children.flatMap { _.allChildLinks }
	
	/**
	 * @return All regular links in this class hierarchy
	 */
	def allRegularLinks: Vector[DisplayedLink] = links ++ children.flatMap { _.allRegularLinks }
	
	/**
	 * @return The child classes directly below this one
	 */
	def children = childLinks.map { _.child }
	
	/**
	 * @return All classes that belong to this class hierarchy
	 */
	def classes: Vector[Class] = classData +: children.flatMap { _.classes }
	
	/**
	 * @return A shrinked copy of this class hierarchy
	 */
	def shrinked: DisplayedClass = if (isExpanded) copy(isExpanded = false, childLinks = childLinks.map {
		_.mapClass { _.shrinked } }) else mapChildren { _.shrinked }
	
	
	// IMPLEMENTED	----------------------
	
	override def info = classData.info
	
	override def attributes = classData.attributes
	
	override def makeCopy(info: ClassInfo, attributes: Vector[Attribute]) =
		copy(classData = classData.copy(info = info, attributes = attributes))
	
	override def toString = s"$classData, links: [${links.mkString(", ")}]"
	
	
	// OTHER	--------------------------
	
	/**
	 * @param searchedClassId The searched class' id
	 * @return Whether this class hierarchy contains the specified id
	 */
	def containsClassWithId(searchedClassId: Int): Boolean = classId == searchedClassId ||
		children.exists { _.containsClassWithId(searchedClassId) }
	
	/**
	 * Tries to detach a child class from this class hierarchy
	 * @param detachedClassId Id of the class to detach
	 * @return A copy of this class without the specified child + the detached child. None if the class doesn't belong
	 *         to this class hierarchy
	 */
	def tryDetach(detachedClassId: Int): Option[(DisplayedClass, DisplayedClass)] =
	{
		children.indexWhereOption { _.classId == detachedClassId } match
		{
			case Some(indexToDetach) => Some(copy(childLinks = childLinks.withoutIndex(indexToDetach)) -> children(indexToDetach))
			case None =>
				// Tries to find the detached child from grandchildren
				children.findMapAndIndex { _.tryDetach(detachedClassId) }.map { case (detachResult, index) =>
					copy(childLinks = childLinks.mapIndex(index) { _.copy(child = detachResult._1) }) -> detachResult._2 }
		}
	}
	
	/**
	 * @param classId A class id
	 * @return A class in this hierarchy with specified id (may be this class). None if no such id exists in this hierarchy.
	 */
	def classForId(classId: Int): Option[DisplayedClass] =
	{
		if (classId == this.classId)
			Some(this)
		else
			childLinks.findMap { _.child.classForId(classId) }
	}
	
	/**
	 * @param classId A linking class id
	 * @return All classes in this hierarchy that can be linked from specified class. Returns an empty vector if
	 *         the specified class belongs to this hierarchy and all hierarchy classes otherwise.
	 */
	def classesLinkableFrom(classId: Int): Vector[Class] =
	{
		// The class hierarchy containing specified class cannot be linked, other classes can be linked freely
		if (containsClassWithId(classId))
			Vector()
		else
			classes
	}
	
	/**
	 * @param expanded Whether the class display should be expanded
	 * @return A copy of this model with specified expand state
	 */
	def withExpandState(expanded: Boolean) = copy(isExpanded = expanded)
	
	/**
	 * Expands a specific class in this hierarchy
	 * @param expandedClassId Id of the expanded class
	 * @return A copy of this hierarchy with targeted class and its parents expanded
	 */
	def withClassExpanded(expandedClassId: Int): DisplayedClass =
	{
		if (containsClassWithId(expandedClassId))
			copy(childLinks = childLinks.map { _.mapClass { _.withClassExpanded(expandedClassId) } }, isExpanded = true)
		else
			this
	}
	
	/**
	 * Shrinks a specific class in this hierarchy (also affects all classes under that class)
	 * @param shrinkedClassId The id of the class to shrink
	 * @return A modified copy of this class hierarchy
	 */
	def withClassShrinked(shrinkedClassId: Int): DisplayedClass =
	{
		if (classId == shrinkedClassId)
			shrinked
		else
			mapChildren { _.withClassShrinked(shrinkedClassId) }
	}
	
	/**
	 * @param newClass New class state to display
	 * @return A copy of this display with specified class
	 */
	def withClass(newClass: Class) = copy(classData = newClass)
	
	/**
	 * Edits a class specification in this hierarchy
	 * @param newClassVersion A new class specification
	 * @return A modified version of this hierarchy
	 */
	def edited(newClassVersion: Class): DisplayedClass =
	{
		// Either updates this class or one of the children
		if (classId == newClassVersion.id)
			copy(classData = newClassVersion)
		else
			copy(childLinks = childLinks.mapFirstWhere { _.child.containsClassWithId(newClassVersion.id) } {
				_.mapClass { _.edited(newClassVersion) } })
	}
	
	/**
	 * @param classIds Ids of classes that should no longer be linked to this hierarchy
	 * @return A copy of this class display without any links to specified class
	 */
	def withoutLinksToClassesWithIds(classIds: Set[Int]): DisplayedClass = copy(links = links.filterNot { l =>
		classIds.contains(l.otherClass.id) }, childLinks = childLinks.map { _.mapClass { _.withoutLinksToClassesWithIds(classIds) } })
	
	/**
	 * @param classId Id of targeted class
	 * @return A copy of this hierarchy with specified class, along with all its child classes, removed
	 */
	def withoutClass(classId: Int): Option[DisplayedClass] =
	{
		if (this.classId == classId)
			None
		else
			Some(copy(childLinks = childLinks.flatMap { l => l.child.withoutClass(classId).map { c => l.copy(child = c) } }))
	}
	
	/**
	 * Adds a new child to this class hierarchy. The child doesn't need to be added directly to this class.
	 * @param link A new child link
	 * @return A copy of this class with specified link added
	 */
	def withChildAdded(link: ChildLink): DisplayedClass =
	{
		// Searches the new owner class from this class and then from children (recursive)
		val ownerId = link.ownerClassId
		if (ownerId.forall { _ == this.classId })
			copy(childLinks = childLinks :+ link)
		else
			copy(childLinks = childLinks.mapFirstWhere { _.child.containsClassWithId(ownerId.get) } { l =>
				l.copy(child = l.child.withChildAdded(link)) })
	}
	
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
		val filteredLinks = links.filterNot { _.link.id == link.id }
		if (filteredLinks.size == links.size)
			this
		else
			copy(links = filteredLinks)
	}
	
	/**
	 * @param attribute An attribute
	 * @return All classes in this hierarchy that use the specified attribute in a mapping link
	 *         (class owning specified attribute is not included)
	 */
	def classesUsingAttributeInLink(attribute: Attribute): Vector[Class] =
	{
		// Only returns classes in this hierarchy, and doesn't include attribute class itself
		if (attribute.classId != classId && links.exists { _.mappingKeyAttributeId.contains(attribute.id) })
			children.flatMap { classData +: _.classesUsingAttributeInLink(attribute) }
		else
			children.flatMap { _.classesUsingAttributeInLink(attribute) }
	}
	
	/**
	 * @param classId Id of a class
	 * @return All classes in this hierarchy that are affected by the specified class. This includes all classes that
	 *         link to specified class, as well as all sub-classes of the specified class.
	 */
	def classesAffectedByClassWithId(classId: Int): Vector[Class] =
	{
		// If this hierarchy contains the specified class, lists all classes under that class
		if (this.classId == classId)
			children.flatMap { _.classes }
		// Also lists all classes that link to specified class
		else
		{
			val childResults = children.flatMap { _.classesAffectedByClassWithId(classId) }
			if (links.exists { _.otherClass.id == classId })
				classData +: childResults
			else
				childResults
		}
	}
	
	/**
	 * @param classId A class id
	 * @return A list of that classes sub-classes within this hierarchy
	 */
	def subClassesOfClassWithId(classId: Int): Vector[Class] =
	{
		if (this.classId == classId)
			children.flatMap { _.classes }
		else
			children.flatMap { _.subClassesOfClassWithId(classId) }
	}
	
	/**
	 * @param classIds A set of class ids
	 * @return All classes in this hierarchy that reference any of the specified classes
	 */
	def classesLinkingAnyOf(classIds: Set[Int]): Vector[Class] =
	{
		val childResult = children.flatMap { _.classesLinkingAnyOf(classIds) }
		if (links.exists { link => classIds.contains(link.otherClass.id) })
			classData +: childResult
		else
			childResult
	}
	
	private def mapChildren(f: DisplayedClass => DisplayedClass) =
	{
		if (children.isEmpty)
			this
		else
			copy(childLinks = childLinks.map { _.mapClass(f) })
	}
}
