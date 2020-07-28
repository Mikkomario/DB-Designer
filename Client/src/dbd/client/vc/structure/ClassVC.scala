package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.{DeleteQuestionDialog, EditClassDialog, EditSubClassDialog}
import dbd.client.model.ParentOrSubClass
import dbd.core.model.existing.database.{Attribute, Class}
import dbd.core.model.template.ClassLike
import dbd.core.util.Log
import utopia.flow.util.CollectionExtensions._
import utopia.genesis.event.{ConsumeEvent, MouseButton}
import utopia.genesis.handling.MouseButtonStateListener
import utopia.genesis.shape.Axis.Y
import utopia.genesis.shape.shape2D.Direction2D.Up
import utopia.genesis.shape.shape2D.Insets
import utopia.reflection.component.template.display.Refreshable
import utopia.reflection.component.context.ColorContext
import utopia.reflection.component.drawing.immutable.BorderDrawer
import utopia.reflection.component.swing.animation.AnimatedVisibility
import utopia.reflection.component.swing.button.{ImageButton, ImageCheckBox}
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.component.swing.template.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.stack.StackLayout.Center
import utopia.reflection.container.swing.layout.multi.Stack
import utopia.reflection.localization.DisplayFunction
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.Border

/**
 * Displays interactive UI for a class
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class ClassVC(initialClass: ParentOrSubClass, classManager: ClassDisplayManager, parentContext: ColorContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[ParentOrSubClass]
{
	// ATTRIBUTES	------------------------
	
	import dbd.client.view.DefaultContext._
	
	private var _content = initialClass
	
	private implicit val languageCode: String = "en"
	private val headerContext = parentContext.inContextWithBackground(colorScheme.primary.forBackground(
		parentContext.containerBackground))
	private val contentContext = parentContext.inContextWithBackground(colorScheme.gray.forBackground(
		parentContext.containerBackground))
	
	private val expandButton = headerContext.use { implicit c => new ImageCheckBox(Icons.expandMore.asIndividualButton,
		Icons.expandLess.asIndividualButton, initialClass.isExpanded) }
	
	private val classNameLabel = headerContext.forTextComponents().mapInsets { _.mapRight { _.expanding } }.use {
		implicit c => ItemLabel.contextual(initialClass.displayedClass.classData, DisplayFunction.noLocalization[Class] { _.info.name }) }
	
	private val header = headerContext.use { implicit hc =>
		Stack.buildRowWithContext(layout = Center) { headerRow =>
			headerRow += expandButton
			headerRow += classNameLabel
			// Adds edit class button
			headerRow += ImageButton.contextual(Icons.edit.asIndividualButton) {
				parentWindow.foreach { window =>
					// Presents different dialog for top-level and sub-level classes
					_content.data match
					{
						case Right(subLevel) =>
							val (parent, link) = subLevel
							new EditSubClassDialog(parent, Some(link), classManager).display(window).foreach { _.foreach {
								case Right(_) => Log.warning("Somehow received add class response from edit sub-class dialog")
								case Left(editResult) => classManager.editSubClass(link, editResult)
							} }
						case Left(topLevel) =>
							val classToEdit = topLevel
							new EditClassDialog(Some(classToEdit.info)).display(window).foreach { _.foreach { editedInfo =>
								classManager.editClass(classToEdit.classData, editedInfo)
							} }
					}
				}
			}
			// Adds delete class button
			headerRow += ImageButton.contextual(Icons.close.asIndividualButton) {
				parentWindow.foreach { window =>
					val classToDelete = displayedClass
					DeleteQuestionDialog.forClass(classToDelete.name,
						classManager.classesAffectedByClassDeletion(classToDelete.classId).toVector).display(window).foreach {
						if (_) classManager.deleteClass(classToDelete) }
				}
			}
		}.framed(margins.small.downscaling x margins.small.any, hc.containerBackground)
	}
	
	private val attributeSection = new AttributesVC(initialClass.displayedClass.classId,
		orderedAttributes(initialClass), classManager)(contentContext)
	private val linksSection = new LinksVC(initialClass.displayedClass, classManager)(contentContext)
	private val subClassSection = new SubClassesVC(initialClass, classManager)(contentContext)
	private val classContentView = contentContext.use { implicit c =>
		Stack.buildColumnWithContext() { stack =>
			stack += attributeSection
			stack += linksSection
			stack += subClassSection
		}.framed(margins.medium.downscaling.square, c.containerBackground)
	}
	private val animatedClassContent = AnimatedVisibility.contextual(classContentView, Y,
		isShownInitially = initialClass.isExpanded)
	
	private val view = Stack.columnWithItems(Vector(header, animatedClassContent), margin = 0.fixed)
	
	
	// INITIAL CODE	------------------------
	
	classContentView.addCustomDrawer(new BorderDrawer(Border(Insets.symmetric(2).withoutSide(Up), colorScheme.primary)))
	expandButton.addValueListener { e => classManager.changeClassExpand(displayedClass.classId, e.newValue) }
	classNameLabel.addMouseButtonListener(MouseButtonStateListener.onButtonPressedInside(MouseButton.Left) {
		classNameLabel.bounds } { _ => { expandButton.value = true; Some(ConsumeEvent("Class expanded")) } })
	
	
	// COMPUTED	----------------------------
	
	def displayedClass = _content.displayedClass
	
	def isExpanded = expandButton.value
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: ParentOrSubClass) =
	{
		// If keeps same class, animates expansion changes
		val isSameClass = _content.classId == newContent.classId
		
		// Content may be shrinked or expanded
		_content = newContent
		
		classNameLabel.content = newContent.displayedClass.classData
		attributeSection.content = newContent.displayedClass.classId -> orderedAttributes(newContent)
		linksSection.content = newContent.displayedClass
		subClassSection.content = newContent
		
		expandButton.value = newContent.isExpanded
		if (isSameClass)
			animatedClassContent.isShown = newContent.isExpanded
		else
			animatedClassContent.setStateWithoutTransition(newContent.isExpanded)
	}
	
	override def content = _content
	
	
	// OTHER	---------------------------
	
	private def orderedAttributes(c: ClassLike[_, Attribute, _]) = c.attributes.sortedWith(Ordering.by { !_.isSearchKey },
		Ordering.by { _.isOptional }, Ordering.by { _.name })
}
