package dbd.client.vc

import dbd.client.model.Fonts
import dbd.core.model
import utopia.genesis.color.Color
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.localization.DisplayFunction
import utopia.reflection.util.{ColorScheme, ComponentContextBuilder}

/**
 * Displays interactive UI for a class
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class ClassVC(initialClass: model.Class)(implicit baseCB: ComponentContextBuilder, fonts: Fonts,
										 colorScheme: ColorScheme, defaultLanguageCode: String)
	extends StackableAwtComponentWrapperWrapper with Refreshable[model.Class]
{
	// ATTRIBUTES	------------------------
	
	// private implicit val baseContext: ComponentContext = baseCB.result
	
	private val header = ItemLabel.contextual(initialClass, DisplayFunction.noLocalization[model.Class] { _.info.name })(
		baseCB.copy(textColor = Color.white, font = fonts.header, background = Some(colorScheme.primary)).result)
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = header
	
	override def content_=(newContent: model.Class) = header.content = newContent
	
	override def content = header.content
}
