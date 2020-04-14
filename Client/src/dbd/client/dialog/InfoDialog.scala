package dbd.client.dialog

import utopia.genesis.image.Image
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.MultiLineTextView
import utopia.reflection.component.swing.label.ImageLabel
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.{LocalizedString, Localizer}
import utopia.reflection.util.{ComponentContext, Screen}

/**
 * Used for displaying some information for the user
 * @author Mikko Hilpinen
 * @since 27.1.2020, v0.1
 * @param title Title of dialog
 * @param text Text displayed on the dialog
 * @param icon icon displayed on the dialog (None if no icon, default)
 */
class InfoDialog(val title: LocalizedString, text: LocalizedString, icon: Option[Image] = None)
				(implicit context: ComponentContext, colorScheme: ColorScheme, localizer: Localizer)
	extends InteractionDialog[Unit]
{
	private implicit val language: String = "en"
	
	override protected def buttonData = Vector(DialogButtonInfo.cancel("OK"))
	
	override protected def dialogContent =
	{
		val textView = MultiLineTextView.contextual(text, Screen.size.width / 3)
		icon match
		{
			case Some(icon) => Stack.buildRowWithContext() { stack =>
				stack += ImageLabel.contextual(icon, alwaysFillsArea = false)
				stack += textView
			}
			case None => textView
		}
	}
	
	override protected def defaultResult = ()
}
