package dbd.client.model

import utopia.genesis.color.Color
import utopia.genesis.image.Image
import utopia.reflection.component.swing.button.ButtonImageSet

/**
 * Used for providing an icon with different settings for buttons or other contexts
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
case class Icon(private val original: Image)
{
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
}
