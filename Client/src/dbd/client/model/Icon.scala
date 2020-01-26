package dbd.client.model

import utopia.genesis.color.Color
import utopia.genesis.image.Image
import utopia.reflection.color.ComponentColor
import utopia.reflection.color.TextColorStandard.{Dark, Light}
import utopia.reflection.component.swing.button.ButtonImageSet

/**
 * Used for providing an icon with different settings for buttons or other contexts
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
case class Icon(private val original: Image)
{
	// ATTRIBUTES	------------------------
	
	/**
	 * A black version of this icon
	 */
	lazy val black = original.withAlpha(0.88)
	/**
	 * A white version of this icon
	 */
	lazy val white = original.withColorOverlay(Color.white)
	/**
	 * A version of this icon for light image + text buttons
	 */
	lazy val forLightButtons = ButtonImageSet.lowAlphaOnDisabled(black, 0.65)
	/**
	 * A version of this icon for dark image + text buttons
	 */
	lazy val forDarkButtons = ButtonImageSet.lowAlphaOnDisabled(white)
	
	
	// COMPUTED	---------------------------
	
	/**
	 * @return A full size version of this icon where icon size matches the source resolution
	 */
	def fullSize =
	{
		val newIcon = original.withOriginalSize
		if (original == newIcon)
			this
		else
			copy(original = newIcon)
	}
	
	
	// OTHER	---------------------------
	
	/**
	 * @param color Target icon color
	 * @return A button image set to be used in buttons without text
	 */
	def forButtonWithoutText(color: Color) = ButtonImageSet.brightening(original.withColorOverlay(color))
	
	/**
	 * @param f A mapping function
	 * @return A mapped copy of this icon
	 */
	def map(f: Image => Image) = Icon(f(original))
	
	/**
	 * @param color Button background color
	 * @return Images used with specified button background
	 */
	def forButtonWithBackground(color: ComponentColor) = color.textColorStandard match
	{
		case Dark => forLightButtons
		case Light => forDarkButtons
	}
	
	/**
	 * @param iconColor New color of the icon
	 * @return An icon image with specified color overlay
	 */
	def asImageWithColor(iconColor: Color) = original.withColorOverlay(iconColor)
}
