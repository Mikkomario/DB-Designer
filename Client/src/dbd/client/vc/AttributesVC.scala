package dbd.client.vc

import dbd.client.controller.Icons
import dbd.client.dialog.EditAttributeDialog
import utopia.reflection.shape.LengthExtensions._
import dbd.core.model.Attribute
import dbd.core.util.Log
import utopia.genesis.shape.Axis.X
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContentManager
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ColorScheme, ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Displays a number of class attributes
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class AttributesVC(implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme,
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
				case Some(window) => new EditAttributeDialog(window).display().foreach {
					case Some(edited) => println(s"New attribute config: $edited") // TODO: Add real attribute handling
					case None => println("No new attribute")
				}
				case None => Log.warning("No parent window available for Add Attribute -dialog")
			}
		}).alignedToSide(Direction2D.Right, useLowPriorityLength = true)
	}.framed(margins.medium.downscaling.square, colorScheme.gray)
	//attributesStack.
	
	
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
			new AttributeRowVC(segmentGroup, _) }
		
		override protected def dropDisplays(dropped: Vector[AttributeRowVC]) =
		{
			dropped.foreach { _.unregister() }
			attributesStack --= dropped
		}
		
		override protected def finalizeRefresh() = attributesStack.revalidate()
		
		override protected def itemsAreEqual(a: Attribute, b: Attribute) = a == b
	}
}
