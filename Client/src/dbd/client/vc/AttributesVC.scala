package dbd.client.vc

import utopia.reflection.shape.LengthExtensions._
import dbd.core.model.Attribute
import utopia.genesis.shape.Axis.X
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContentManager
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ColorScheme, ComponentContextBuilder}

/**
 * Displays a number of class attributes
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class AttributesVC(implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Vector[Attribute]]
{
	// ATTRIBUTES	-----------------------
	
	private val segmentGroup = new SegmentedGroup(X)
	private val stack = Stack.column[AttributeRowVC]()
	
	private val view = stack.framed(margins.medium.downscaling.square, colorScheme.gray)
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Vector[Attribute]) = AttributesManager.content = newContent
	
	override def content = AttributesManager.content
	
	
	// NESTED	---------------------------
	
	private object AttributesManager extends ContentManager[Attribute, AttributeRowVC]
	{
		// IMPLEMENTED	-------------------
		
		override def displays = stack.components
		
		override protected def addDisplaysFor(values: Vector[Attribute]) = stack ++= values.map {
			new AttributeRowVC(segmentGroup, _) }
		
		override protected def dropDisplays(dropped: Vector[AttributeRowVC]) =
		{
			dropped.foreach { _.unregister() }
			stack --= dropped
		}
		
		override protected def finalizeRefresh() = stack.revalidate()
		
		override protected def itemsAreEqual(a: Attribute, b: Attribute) = a == b
	}
}
