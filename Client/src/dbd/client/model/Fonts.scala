package dbd.client.model

import utopia.reflection.text.Font

/**
 * A set of fonts for the UI
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 * @param standard The standard (default) font
 */
case class Fonts(standard: Font)
{
	/**
	 * A highlighted (bold) version of the standard font
	 */
	lazy val highlighted = standard.bold
	
	/**
	 * A version of the standard font suitable for headers
	 */
	lazy val header = (standard * 1.2).bold
}
