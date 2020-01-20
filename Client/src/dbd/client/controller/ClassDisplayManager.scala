package dbd.client.controller

import dbd.client.model.{DisplayedClass, DisplayedLink}
import utopia.flow.util.CollectionExtensions._
import dbd.core.database
import dbd.core.model.existing.{Attribute, Class, Link}
import dbd.core.model.partial.{NewAttribute, NewAttributeConfiguration, NewClass, NewClassInfo}
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
	 * Moves the opened class next to the triggering class and expands it
	 * @param triggerClassId Id of triggering class
	 * @param openedClassId Id of opened class
	 */
	def openLink(triggerClassId: Int, openedClassId: Int) =
	{
		// Moves the opened class next to the triggering class and expands it
		val cachedClasses = content
		cachedClasses.indexWhereOption { _.classId == triggerClassId }.foreach { triggerIndex =>
			cachedClasses.indexWhereOption { _.classId == openedClassId }.foreach { openedIndex =>
				val firstCutIndex = triggerIndex min openedIndex
				val secondCutIndex = triggerIndex max openedIndex
				val beginning = cachedClasses.take(firstCutIndex)
				val end = cachedClasses.slice(firstCutIndex + 1, secondCutIndex) ++ cachedClasses.drop(secondCutIndex + 1)
				
				val triggerClass = cachedClasses(triggerIndex)
				val openedClass = cachedClasses(openedIndex).withExpandState(true)
				
				content = (beginning :+ triggerClass :+ openedClass) ++ end
			}
		}
	}
	
	/**
	 * Adds a completely new class
	 * @param newClass New class to add
	 */
	def addNewClass(newClass: NewClass) = editClass { implicit connection => database.Classes.insert(newClass) }
	
	/**
	 * Adds a completely new class
	 * @param newClassInfo Info for the new class
	 */
	def addNewClass(newClassInfo: NewClassInfo): Unit = addNewClass(NewClass(newClassInfo, Vector()))
	
	/**
	 * Deletes a class
	 */
	def deleteClass(classToDelete: Class): Unit =
	{
		ConnectionPool.tryWith { implicit connection =>
			database.Class(classToDelete.id).markDeleted()
		} match
		{
			case Success(wasDeleted) =>
				if (wasDeleted)
					content = content.filterNot { _.classData.id == classToDelete.id }
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
		val cachedContent = content
		cachedContent.indexWhereOption { _.classData.id == targetClassId }.foreach { index =>
			val existingVersion = cachedContent(index)
			if (existingVersion.isExpanded != newExpandState)
				content = cachedContent.updated(index, existingVersion.withExpandState(newExpandState))
		}
	}
	
	/**
	 * Expands specified class in view
	 * @param targetClassId Id of Targeted class
	 */
	def expandClass(targetClassId: Int) = changeClassExpand(targetClassId, newExpandState = true)
	
	/**
	 * Shrinks specified class in view
	 * @param targetClassId Id of Targeted class
	 */
	def shrinkClass(targetClassId: Int) = changeClassExpand(targetClassId, newExpandState = false)
	
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
	
	private def editAttributes[R](classId: Int)(databaseAction: Connection => R)(modifyClass: (Class, R) => Class) =
	{
		// Finds targeted class, performs database modification + class modification and finally updates class in displays
		content.find { _.classId == classId }.foreach { classToEdit => editClass { connection =>
			modifyClass(classToEdit.classData, databaseAction(connection)) } }
	}
	
	private def editClass(databaseAction: Connection => Class) =
	{
		ConnectionPool.tryWith(databaseAction) match
		{
			case Success(editedClass) =>
				// Either replaces an existing class or adds a new one
				val cachedContent = content
				cachedContent.indexWhereOption { _.classId == editedClass.id } match
				{
					case Some(editedIndex) =>
						content = cachedContent.updated(editedIndex, cachedContent(editedIndex).withClass(editedClass))
					case None => content :+= DisplayedClass(editedClass, isExpanded = true)
				}
			case Failure(error) => Log(error, "Failed to modify a class")
		}
	}
	
	private def pairData(classData: Vector[Class], linkData: Vector[Link]) =
	{
		classData.map { c =>
			val fromLinks = linkData.filter { _.originClassId == c.id }.flatMap { l => classData.find {
				_.id == l.targetClassId }.map { otherClass => DisplayedLink(l, otherClass) } }
			val toLinks = linkData.filter { _.targetClassId == c.id }.flatMap { l => classData.find {
				_.id == l.originClassId }.map { otherClass => DisplayedLink(l, otherClass) } }
			
			DisplayedClass(c, fromLinks ++ toLinks)
		}.sortBy { _.name }
	}
}
