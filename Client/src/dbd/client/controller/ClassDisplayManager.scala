package dbd.client.controller

import dbd.client.model.{ChildLink, DisplayedClass, DisplayedLink}
import utopia.flow.util.CollectionExtensions._
import dbd.core.database
import dbd.core.model.enumeration.LinkEndRole
import dbd.core.model.enumeration.LinkEndRole.{Origin, Target}
import dbd.core.model.existing.{Attribute, Class, Link}
import dbd.core.model.partial.{NewAttribute, NewAttributeConfiguration, NewClass, NewClassInfo, NewLinkConfiguration}
import dbd.core.util.Log
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.component.RefreshableWithPointer
import utopia.vault.database.Connection

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Keeps track of which classes are displayed, how and whether they should be expanded or not
 * @author Mikko Hilpinen
 * @since 18.1.2020, v0.1
 */
class ClassDisplayManager(classesToDisplay: Vector[Class] = Vector(), linksToDisplay: Vector[Link] = Vector())
						 (implicit exc: ExecutionContext)
	extends RefreshableWithPointer[Vector[DisplayedClass]]
{
	// ATTRIBUTES	-----------------------
	
	override val contentPointer = new PointerWithEvents(pairData(classesToDisplay, linksToDisplay))
	
	
	// OTHER	---------------------------
	
	/**
	 * @param linkingClassId Id of the linking class
	 * @return A list of classes that can be linked with specified class
	 */
	def linkableClasses(linkingClassId: Int) = content.flatMap { _.classesLinkableFrom(linkingClassId) }
	
	/**
	 * Moves the opened class next to the triggering class and expands it
	 * @param triggerClassId Id of triggering class
	 * @param openedClassId Id of opened class
	 */
	def openLink(triggerClassId: Int, openedClassId: Int) =
	{
		// Moves the opened class next to the triggering class and expands it
		val cachedClasses = content
		cachedClasses.indexWhereOption { _.containsClassWithId(triggerClassId) }.foreach { triggerIndex =>
			cachedClasses.indexWhereOption { _.containsClassWithId(openedClassId) }.foreach { openedIndex =>
				val firstCutIndex = triggerIndex min openedIndex
				val secondCutIndex = triggerIndex max openedIndex
				val beginning =
				{
					val alwaysTake = cachedClasses.take(firstCutIndex)
					if (triggerIndex > openedIndex)
						alwaysTake ++ cachedClasses.slice(openedIndex + 1, triggerIndex)
					else
						alwaysTake
				}
				val end =
				{
					val alwaysTake = cachedClasses.drop(secondCutIndex + 1)
					if (openedIndex > triggerIndex)
						cachedClasses.slice(triggerIndex + 1, openedIndex) ++ alwaysTake
					else
						alwaysTake
				}
				
				val triggerClass = cachedClasses(triggerIndex)
				val openedClass = cachedClasses(openedIndex).withClassExpanded(openedClassId)
				
				content = (beginning :+ triggerClass :+ openedClass) ++ end
			}
		}
	}
	
	/**
	 * Adds a completely new class
	 * @param newClass New class to add
	 */
	def addNewClass(newClass: NewClass) =
	{
		// Inserts a new class to DB and then to the end of displayed classes list
		ConnectionPool.tryWith { implicit connection => database.Classes.insert(newClass) } match
		{
			case Success(newClass) => content = content :+ DisplayedClass(newClass, isExpanded = true)
			case Failure(error) => Log(error, s"Failed to insert class $newClass to DB")
		}
	}
	
	/**
	 * Adds a completely new class
	 * @param newClassInfo Info for the new class
	 */
	def addNewClass(newClassInfo: NewClassInfo): Unit = addNewClass(NewClass(newClassInfo, Vector()))
	
	/**
	 * Deletes a class
	 */
	def deleteClass(classToDelete: DisplayedClass): Unit =
	{
		// Deletes the class and all its children from DB
		val classIdsToDelete = classToDelete.classIds
		ConnectionPool.tryWith { implicit connection =>
			classIdsToDelete.foreach { cId => database.Class(cId).markDeleted() }
		} match
		{
			case Success(_) =>
				// Deletes the class from displayed classes and removes any links pointing to it
				content = content.flatMap { _.withoutClass(classToDelete.classId) }.map {
					_.withoutLinksToClassesWithIds(classIdsToDelete) }
			case Failure(error) => Log(error, s"Failed to delete class $classToDelete")
		}
	}
	
	/**
	 * Expands or shrinks a class (affects display)
	 * @param targetClassId Id of Targeted class
	 * @param newExpandState Whether the class should be expanded (true) or shrinked (false)
	 */
	def changeClassExpand(targetClassId: Int, newExpandState: Boolean): Unit =
	{
		if (newExpandState)
			expandClass(targetClassId)
		else
			shrinkClass(targetClassId)
	}
	
	/**
	 * Expands specified class in view
	 * @param targetClassId Id of Targeted class
	 */
	def expandClass(targetClassId: Int) = content = content.mapFirstWhere {
		_.containsClassWithId(targetClassId) } { _.withClassExpanded(targetClassId) }
	
	/**
	 * Shrinks specified class in view
	 * @param targetClassId Id of Targeted class
	 */
	def shrinkClass(targetClassId: Int) = content = content.mapFirstWhere {
		_.containsClassWithId(targetClassId) } { _.withClassShrinked(targetClassId) }
	
	/**
	 * Edits specified class' info
	 * @param classToEdit Targeted class
	 * @param editedInfo New info for the class
	 */
	def editClass(classToEdit: Class, editedInfo: NewClassInfo): Unit = editClass { implicit connection =>
		classToEdit.update(database.Class(classToEdit.id).info.update(editedInfo)) }
	
	/**
	 * Adds a new attribute to a class
	 * @param classId Id of targeted class
	 * @param attribute Attribute to add to the class
	 */
	def addNewAttribute(classId: Int, attribute: NewAttribute): Unit =
	{
		// Inserts attribute data to DB, then updates this view
		editAttributes(classId) { implicit connection => database.Class(classId).attributes.insert(attribute) } { _ + _ }
	}
	
	/**
	 * Edits a class attribute
	 * @param attribute Attribute to edit
	 * @param edit New configuration for the attribute
	 */
	def editAttribute(attribute: Attribute, edit: NewAttributeConfiguration): Unit =
	{
		// Updates attribute data to DB, then updates this view
		editAttributes(attribute.classId) { implicit connection =>
			database.Class(attribute.classId).attribute(attribute.id).configuration.update(edit) } { _.update(_) }
	}
	
	/**
	 * Deletes a class attribute
	 * @param attribute Attribute to delete
	 */
	def deleteAttribute(attribute: Attribute): Unit =
	{
		// Deletes attribute from DB, then from this class
		editAttributes(attribute.classId) { implicit connection =>
			database.Class(attribute.classId).attribute(attribute.id).markDeleted() } { (c, _) => c - attribute }
	}
	
	/**
	 * Adds an entirely new link between two classes
	 * @param link A new link to add
	 */
	def addNewLink(link: NewLinkConfiguration) =
	{
		// Inserts the new link to database, then updates displayed classes
		ConnectionPool.tryWith { implicit connection =>
			database.Links.insert(link)
		} match
		{
			case Success(newLink) => displaysWithLinkAdded(content, newLink).foreach { content = _ }
			case Failure(error) => Log(error, s"Failed to insert link: $link")
		}
	}
	
	/**
	 * Edits an existing link
	 * @param link Targeted link
	 * @param newConfiguration A new configuration for the specified link
	 */
	def editLink(link: Link, newConfiguration: NewLinkConfiguration) =
	{
		ConnectionPool.tryWith { implicit connection =>
			database.Link(link.id).configuration.update(newConfiguration)
		} match
		{
			case Success(updatedConfiguration) =>
				// Removes the old link, then adds the new one
				val withoutLink = currentDisplaysWithoutLink(link)
				content = displaysWithLinkAdded(withoutLink, link.withConfiguration(updatedConfiguration)).getOrElse(withoutLink)
			
			case Failure(error) => Log(error, s"Failed to edit link $link with $newConfiguration")
		}
	}
	
	/**
	 * Deletes an existing link
	 * @param linkToDelete The link to delete permanently
	 */
	def deleteLink(linkToDelete: Link) =
	{
		ConnectionPool.tryWith { implicit connection =>
			database.Link(linkToDelete.id).markDeleted()
		} match
		{
			case Success(wasDeleted) => if (wasDeleted) content = currentDisplaysWithoutLink(linkToDelete)
			case Failure(error) => Log(error, s"Failed to delete link $linkToDelete")
		}
	}
	
	private def editAttributes[R](classId: Int)(databaseAction: Connection => R)(modifyClass: (Class, R) => Class) =
	{
		// Finds targeted class, performs database modification + class modification and finally updates class in displays
		content.findMap { _.classForId(classId) }.foreach { classToEdit => editClass { connection =>
			modifyClass(classToEdit.classData, databaseAction(connection)) } }
	}
	
	private def editClass(databaseAction: Connection => Class) =
	{
		ConnectionPool.tryWith(databaseAction) match
		{
			case Success(editedClass) =>
				// Either replaces an existing class or adds a new one
				val cachedContent = content
				cachedContent.indexWhereOption { _.containsClassWithId(editedClass.id) } match
				{
					case Some(editedIndex) =>
						content = cachedContent.updated(editedIndex, cachedContent(editedIndex).edited(editedClass))
					case None => content :+= DisplayedClass(editedClass, isExpanded = true)
				}
			case Failure(error) => Log(error, "Failed to modify a class")
		}
	}
	
	private def pairData(classData: Vector[Class], linkData: Vector[Link]) =
	{
		val (regularLinks, childLinks) = linkData.divideBy { _.isOwned }
		
		val paired = classData.map { c =>
			val fromLinks = regularLinks.filter { _.originClassId == c.id }.flatMap { l => classData.find {
				_.id == l.targetClassId }.map { otherClass => DisplayedLink(l, otherClass) } }
			val toLinks = regularLinks.filter { _.targetClassId == c.id }.flatMap { l => classData.find {
				_.id == l.originClassId }.map { otherClass => DisplayedLink(l, otherClass) } }
			
			DisplayedClass(c, fromLinks ++ toLinks)
		}
		formHierarchies(paired, childLinks).sortBy { _.name }
	}
	
	private def formHierarchies(classes: Vector[DisplayedClass], childLinks: Vector[Link]): Vector[DisplayedClass] =
	{
		// Finds which of the classes should be placed lower in the hierarchy
		var childrenWithLinks = childLinks.flatMap { link => link.childClassId.flatMap { cId => classes.find {
			_.classId == cId } }.map { c => ChildLink(c, link) } }
		var parents = classes.filterNot { c => childrenWithLinks.exists { _.child.classId == c.classId } }
		
		// Assigns classes to the hierarchy
		while (childrenWithLinks.nonEmpty)
		{
			// The children that couldn't be assigned are still kept
			val childrenLeftBeforeUpdate = childrenWithLinks.size
			childrenWithLinks = childrenWithLinks.filter { childLink =>
				parents.indexWhereOption { _.containsClassWithId(childLink.ownerClassId.get) } match
				{
					case Some(parentIndex) =>
						parents = parents.mapIndex(parentIndex) { _.withChildAdded(childLink) }
						false
					case None => true
				}
			}
			// If none of the remaining children could be assigned (some parents not included in classes),
			// adds those children as base classes
			if (childrenWithLinks.size == childrenLeftBeforeUpdate)
			{
				parents ++= childrenWithLinks.map { _.child }
				childrenWithLinks = Vector()
			}
		}
		
		parents
	}
	
	private def currentDisplaysWithoutLink(link: Link) =
	{
		// If the link was a parent-child link, has to detach the child and add it back to base classes
		if (link.isOwned)
		{
			val childId = link.childClassId.get
			content.findMapAndIndex { _.tryDetach(childId) }.map { case (detachResult, affectedIndex) =>
				(content.take(affectedIndex) :+ detachResult._1 :+ detachResult._2) ++ content.drop(affectedIndex + 1)
			}.getOrElse(content)
		}
		else
			content.map { _.withoutLink(link) }
	}
	
	// Returns none if no change could be made
	private def displaysWithLinkAdded(displays: Vector[DisplayedClass], newLink: Link) =
	{
		displays.indexWhereOption { _.classIds.contains(newLink.originClassId) }.flatMap { originIndex =>
			displays.indexWhereOption { _.classIds.contains(newLink.targetClassId) }.map { targetIndex =>
				val originClass = displays(originIndex)
				val targetClass = displays(targetIndex)
				// In case of owned links, one of the classes must be moved under another
				if (newLink.isOwned)
				{
					val affectedClasses = Map[LinkEndRole, (DisplayedClass, Int)](
						Origin -> (originClass -> originIndex), Target -> (targetClass -> targetIndex))
					val (newParentClass, updatedIndex) = affectedClasses(newLink.linkType.fixedOwner)
					val (newChildClass, removedIndex) = affectedClasses(newLink.linkType.fixedChild)
					
					displays.updated(updatedIndex, newParentClass.withChildAdded(ChildLink(newChildClass, newLink)))
						.withoutIndex(removedIndex)
				}
				else
				{
					// Updates both origin and target class displays
					displays.updated(originIndex,
						originClass.withLinkAdded(DisplayedLink(newLink, targetClass.classData))).updated(
						targetIndex, targetClass.withLinkAdded(DisplayedLink(newLink, originClass.classData)))
				}
			}
		}
	}
}
