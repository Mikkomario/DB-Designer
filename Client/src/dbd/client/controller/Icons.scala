package dbd.client.controller

import java.nio.file.Path

import dbd.client.model.Icon
import utopia.flow.util.FileExtensions._
import utopia.flow.util.TimeExtensions._
import utopia.flow.caching.multi.ReleasingCache
import utopia.genesis.image.Image
import utopia.genesis.shape.shape2D.Size

/**
 * Used for reading and storing image icons
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object Icons
{
	// ATTRIBUTES	--------------------------
	
	private val iconSourceDir: Path = "Client/images/icons" // TODO: Handle this path better in production
	private val standardIconSize = Size.square(32)
	
	private val cache = ReleasingCache[String, Icon](3.minutes) { imgName => Icon(Image.readOrEmpty(iconSourceDir/imgName)
		.smallerThan(standardIconSize)) }
	
	
	// COMPUTED	------------------------------
	
	/**
	 * @return Circular check icon
	 */
	def checkCircle = apply("check_circle.png")
	
	/**
	 * @return Close icon
	 */
	def close = apply("close.png")
	
	/**
	 * @return Decimal number icon
	 */
	def decimalNumber = apply("decimal.png")
	
	/**
	 * @return Numbers icon
	 */
	def numbers = apply("numbers.png")
	
	/**
	 * @return Text icon
	 */
	def text = apply("text.png")
	
	/**
	 * @return Rectangular icon for adding new items
	 */
	def addBox = apply("add_box.png")
	
	/**
	 * @return An icon for edit option
	 */
	def edit = apply("edit.png")
	
	
	// OTHER	------------------------------
	
	/**
	 * @param iconName Name of searched icon (including file extension)
	 * @return An icon with specified name
	 */
	def apply(iconName: String) = cache(iconName)
}
