package dbd.client.vc

import utopia.flow.util.CollectionExtensions._
import dbd.core.database
import dbd.client.controller.{ConnectionPool, Icons}
import dbd.client.dialog.EditClassDialog
import dbd.client.model.Fonts
import utopia.reflection.shape.LengthExtensions._
import dbd.core.model.existing.{Attribute, Class}
import dbd.core.model.partial.{NewAttribute, NewAttributeConfiguration, NewClass, NewClassInfo}
import dbd.core.util.Log
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.{CollectionView, Stack}
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.ComponentContextBuilder
import utopia.vault.database.Connection

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Used for displaying data about multiple classes at a time
 * @author Mikko Hilpinen
 * @since 17.1.2020, v0.1
 */
class ClassesVC(targetHeight: Double)
			   (implicit margins: Margins, baseCB: ComponentContextBuilder, fonts: Fonts, colorScheme: ColorScheme,
				defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Vector[(Class, Boolean)]]
{
	// ATTRIBUTES	---------------------
	
	private val addClassButton = ImageAndTextButton.contextual(Icons.addBox.forButtonWithBackground(colorScheme.secondary.dark),
		"Add Class") { () => addButtonPressed() }(baseCB.withColors(colorScheme.secondary.dark).result)
	private val classView = new CollectionView[ClassVC](Direction2D.Down, targetHeight, margins.medium.downscaling)
	private val manager = new ContainerContentManager[(Class, Boolean), CollectionView[ClassVC], ClassVC](classView)(
		c => new ClassVC(c._1, c._2)(newAttributeAdded)(attributeEdited)(attributeDeleted)(classEdited)(classExpandChanged))
	private val view = Stack.buildColumnWithContext() { stack =>
		stack += classView
		stack += addClassButton.alignedToSide(Direction2D.Right, useLowPriorityLength = true)
	}(baseCB.result)
	
	
	// IMPLEMENTED	---------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Vector[(Class, Boolean)]) = manager.content = newContent
	
	override def content = manager.content
	
	
	// OTHER	-------------------------
	
	private def addButtonPressed() =
	{
		parentWindow.foreach { window =>
			new EditClassDialog().display(window).foreach { _.foreach { newClassInfo =>
				editClass { implicit connection => database.Classes.insert(NewClass(newClassInfo, Vector())) }
			} }
		}
	}
	
	private def classExpandChanged(targetClass: Class, newExpandState: Boolean): Unit =
	{
		val cachedContent = content
		cachedContent.indexWhereOption { _._1.id == targetClass.id }.foreach { index =>
			if (cachedContent(index)._2 != newExpandState)
				content = cachedContent.updated(index, targetClass -> newExpandState)
		}
	}
	
	private def classEdited(classToEdit: Class, editedInfo: NewClassInfo): Unit = editClass { implicit connection =>
		classToEdit.update(database.Class(classToEdit.id).info.update(editedInfo)) }
	
	private def newAttributeAdded(classId: Int, attribute: NewAttribute): Unit =
	{
		// Inserts attribute data to DB, then updates this view
		editAttributes(classId) { implicit connection => database.Class(classId).attributes.insert(attribute) } { _ + _ }
	}
	
	private def attributeEdited(attribute: Attribute, edit: NewAttributeConfiguration): Unit =
	{
		// Updates attribute data to DB, then updates this view
		editAttributes(attribute.classId) { implicit connection =>
			database.Class(attribute.classId).attribute(attribute.id).configuration.update(edit) } { _.update(_) }
	}
	
	private def attributeDeleted(attribute: Attribute): Unit =
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
