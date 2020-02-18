package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditAttributeDialog
import dbd.client.vc.GroupHeader
import dbd.core.model.existing.Attribute
import dbd.core.model.partial.NewAttribute
import utopia.genesis.shape.Axis.X
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContentManager
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Displays a number of class attributes
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class AttributesVC(initialClassId: Int, initialAttributes: Vector[Attribute] = Vector(), classManager: ClassDisplayManager)
				  (implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme,
				   defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[(Int, Vector[Attribute])]
{
	// ATTRIBUTES	-----------------------
	
	private var classId = initialClassId
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val segmentGroup = new SegmentedGroup(X)
	private val attributesStack = Stack.column[AttributeRowVC](margin = margins.small.downscaling)
	
	private val view = Stack.buildColumnWithContext(isRelated = true) { mainStack =>
		mainStack += GroupHeader("Attributes")
		mainStack += attributesStack
		mainStack += ImageAndTextButton.contextual(Icons.addBox.forLightButtons, "Add Attribute")(() =>
		{
			parentWindow.foreach { window =>
				// Remembers the class for which the attribute is being added
				val editedClassId = classId
				new EditAttributeDialog().display(window).foreach { _.foreach { added =>
					classManager.addNewAttribute(editedClassId, NewAttribute(added))
				} }
			}
		}).alignedToSide(Direction2D.Right, useLowPriorityLength = true)
	}
	
	
	// INITIAL CODE	-----------------------
	
	AttributesManager.content = initialAttributes
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: (Int, Vector[Attribute])) =
	{
		classId = newContent._1
		AttributesManager.content = newContent._2
	}
	
	override def content = classId -> AttributesManager.content
	
	
	// NESTED	---------------------------
	
	private object AttributesManager extends ContentManager[Attribute, AttributeRowVC]
	{
		// IMPLEMENTED	-------------------
		
		override def displays = attributesStack.components
		
		override protected def addDisplaysFor(values: Vector[Attribute]) = attributesStack ++= values.map {
			new AttributeRowVC(segmentGroup, _, classManager) }
		
		override protected def dropDisplays(dropped: Vector[AttributeRowVC]) =
		{
			dropped.foreach { _.unregister() }
			attributesStack --= dropped
		}
		
		override protected def finalizeRefresh() = attributesStack.revalidate()
		
		override protected def itemsAreEqual(a: Attribute, b: Attribute) = a == b
	}
}
