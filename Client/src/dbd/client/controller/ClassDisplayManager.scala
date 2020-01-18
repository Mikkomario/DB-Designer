package dbd.client.controller

import utopia.flow.util.CollectionExtensions._
import dbd.core.database
import dbd.core.model.existing.{Attribute, Class}
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
class ClassDisplayManager(classesToDisplay: Vector[Class] = Vector())(implicit exc: ExecutionContext)
	extends RefreshableWithPointer[Vector[(Class, Boolean)]]
{
	// ATTRIBUTES	-----------------------
	
	override val contentPointer = new PointerWithEvents(classesToDisplay.map { _ -> false })
	
	
	// OTHER	---------------------------
	
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
					content = content.filterNot { _._1.id == classToDelete.id }
			case Failure(error) => Log(error, s"Failed to delete class $classToDelete")
		}
	}
	
	/**
	 * Expands or shrinks a class (affects display)
	 * @param targetClass Targeted class
	 * @param newExpandState Whether the class should be expanded (true) or shrinked (false)
	 */
	def changeClassExpand(targetClass: Class, newExpandState: Boolean): Unit =
	{
		val cachedContent = content
		cachedContent.indexWhereOption { _._1.id == targetClass.id }.foreach { index =>
			if (cachedContent(index)._2 != newExpandState)
				content = cachedContent.updated(index, targetClass -> newExpandState)
		}
	}
	
	/**
	 * Expands specified class in view
	 * @param targetClass Class to expand
	 */
	def expandClass(targetClass: Class) = changeClassExpand(targetClass, newExpandState = true)
	
	/**
	 * Shrinks specified class in view
	 * @param targetClass Class to shrink
	 */
	def shrinkClass(targetClass: Class) = changeClassExpand(targetClass, newExpandState = false)
	
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
		content.find { _._1.id == classId }.foreach { case (classToEdit, _) => editClass { connection =>
			modifyClass(classToEdit, databaseAction(connection)) } }
	}
	
	private def editClass(databaseAction: Connection => Class) =
	{
		ConnectionPool.tryWith(databaseAction) match
		{
			case Success(editedClass) =>
				// Either replaces an existing class or adds a new one
				val cachedContent = content
				cachedContent.indexWhereOption { _._1.id == editedClass.id } match
				{
					case Some(editedIndex) =>
						content = cachedContent.updated(editedIndex, editedClass -> cachedContent(editedIndex)._2)
					case None => content :+= editedClass -> true
				}
			case Failure(error) => Log(error, "Failed to modify a class")
		}
	}
}
