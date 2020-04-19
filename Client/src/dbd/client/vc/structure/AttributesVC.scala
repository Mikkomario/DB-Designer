package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditAttributeDialog
import dbd.client.vc.GroupHeader
import dbd.core.model.existing.Attribute
import dbd.core.model.partial.NewAttribute
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.genesis.color.Color
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
class AttributesVC(initialClassId: Int, initialAttributes: Vector[Attribute] = Vector(),
				   classManager: ClassDisplayManager, parentBackground: Color)
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
		// ATTRIBUTES	-------------------
		
		override val contentPointer = new PointerWithEvents[Vector[Attribute]](initialAttributes)
		
		
		// INITIAL CODE	-------------------
		
		setup()
		
		
		// IMPLEMENTED	-------------------
		
		override protected def representSameItem(a: Attribute, b: Attribute) = a.id == b.id
		
		override protected def contentIsStateless = false
		
		override def displays = attributesStack.components
		
		override protected def addDisplaysFor(values: Vector[Attribute], index: Int) = attributesStack.insertMany(values.map {
			new AttributeRowVC(segmentGroup, _, classManager, parentBackground) }, index)
		
		override protected def dropDisplaysAt(range: Range) = attributesStack.removeComponentsIn(range)
			.foreach { _.unregister() }
		
		override protected def finalizeRefresh() = attributesStack.revalidate()
		
		override protected def itemsAreEqual(a: Attribute, b: Attribute) = a == b
	}
}
