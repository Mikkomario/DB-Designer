package dbd.client.vc

import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.Fonts
import dbd.core.model.existing.Class
import utopia.genesis.color.Color
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ColorScheme, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Displays interactive UI for a class
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class ClassVC(initialClass: Class)
			 (implicit baseCB: ComponentContextBuilder, fonts: Fonts, margins: Margins, colorScheme: ColorScheme,
			  defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Class]
{
	// ATTRIBUTES	------------------------
	
	// private implicit val baseContext: ComponentContext = baseCB.result
	
	private val header = ItemLabel.contextual(initialClass, DisplayFunction.noLocalization[Class] { _.info.name })(
		baseCB.copy(textColor = Color.white, font = fonts.header, background = Some(colorScheme.primary)).result)
	private val attributeSection = new AttributesVC
	
	private val view = Stack.columnWithItems(Vector(header, attributeSection), margin = 0.fixed)
	
	
	// INITIAL CODE	------------------------
	
	// TODO: Add ordering
	attributeSection.content = initialClass.attributes
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Class) =
	{
		header.content = newContent
		attributeSection.content = newContent.attributes // TODO: Add ordering
	}
	
	override def content = header.content
}
