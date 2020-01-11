package dbd.client.controller

import java.nio.file.Path

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
	
	private val cache = ReleasingCache[String, Image](3.minutes) { imgName => Image.readOrEmpty(iconSourceDir/imgName)
		.smallerThan(standardIconSize) }
	
	
	// OTHER	------------------------------
	
	/**
	 * @param iconName Name of searched icon (including file extension)
	 * @return An icon with specified name
	 */
	def apply(iconName: String) = cache(iconName)
}
