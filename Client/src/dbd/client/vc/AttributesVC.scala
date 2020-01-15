package dbd.client.vc

import dbd.client.controller.Icons
import dbd.client.dialog.EditAttributeDialog
import dbd.core.model.existing.Attribute
import dbd.core.model.partial.{NewAttribute, NewAttributeConfiguration}
import utopia.reflection.shape.LengthExtensions._
import dbd.core.util.Log
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
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Displays a number of class attributes
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class AttributesVC(onNewAttribute: NewAttribute => Unit)(onAttributeEdit: (Attribute, NewAttributeConfiguration) => Unit)
				  (onAttributeDeleted: Attribute => Unit)
				  (implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme,
				   defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Vector[Attribute]]
{
	// ATTRIBUTES	-----------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val segmentGroup = new SegmentedGroup(X)
	private val attributesStack = Stack.column[AttributeRowVC]()
	
	private val view = Stack.buildColumnWithContext() { mainStack =>
		mainStack += attributesStack
		mainStack += ImageAndTextButton.contextual(Icons.addBox.forLightButtons, "Add Attribute")(() =>
		{
			parentWindow match
			{
					// TODO: Handle cases where class changes while editing or adding attributes
				case Some(window) => new EditAttributeDialog().display(window).foreach { _.foreach { added =>
					onNewAttribute(NewAttribute(added)) }
				}
				case None => Log.warning("No parent window available for Add Attribute -dialog")
			}
		}).alignedToSide(Direction2D.Right, useLowPriorityLength = true)
	}.framed(margins.medium.downscaling.square, colorScheme.gray.dark)
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Vector[Attribute]) = AttributesManager.content = newContent
	
	override def content = AttributesManager.content
	
	
	// NESTED	---------------------------
	
	private object AttributesManager extends ContentManager[Attribute, AttributeRowVC]
	{
		// IMPLEMENTED	-------------------
		
		override def displays = attributesStack.components
		
		override protected def addDisplaysFor(values: Vector[Attribute]) = attributesStack ++= values.map {
			new AttributeRowVC(segmentGroup, _)(onAttributeEdit)(onAttributeDeleted) }
		
		override protected def dropDisplays(dropped: Vector[AttributeRowVC]) =
		{
			dropped.foreach { _.unregister() }
			attributesStack --= dropped
		}
		
		override protected def finalizeRefresh() = attributesStack.revalidate()
		
		override protected def itemsAreEqual(a: Attribute, b: Attribute) = a == b
	}
}
