package dbd.client.controller

import java.nio.file.Path

import dbd.client.model.Icon
import dbd.core.model.enumeration.{AttributeType, LinkTypeCategory}
import dbd.core.model.enumeration.AttributeType.{BooleanType, DoubleType, InstantType, IntType, ShortStringType}
import dbd.core.model.enumeration.LinkTypeCategory.{ManyToMany, ManyToOne, OneToOne}
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
	
	private val empty = Icon(Image.empty)
	
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
	 * @return A warning icon
	 */
	def warning = apply("warning.png")
	
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
	 * @return Time icon
	 */
	def time = apply("time.png")
	
	/**
	  * @return Icon for adding new items
	  */
	def add = apply("add.png")
	
	/**
	 * @return Rectangular icon for adding new items
	 */
	def addBox = apply("add_box.png")
	
	/**
	 * @return Check box icon
	 */
	def checkBox = apply("check_box.png")
	
	/**
	 * @return An icon for editing items
	 */
	def edit = apply("edit.png")
	
	/**
	 * @return An icon for deleting items
	 */
	def delete = apply("delete.png")
	
	/**
	 * @return An icon for expanding collections
	 */
	def expandMore = apply("expand.png")
	
	/**
	 * @return An icon for closing collections
	 */
	def expandLess = expandMore.map { _.flippedVertically }
	
	/**
	  * @return Icon for displaying more options or information
	  */
	def more = apply("more.png")
	
	/**
	  * @return An icon for uploading to cloud
	  */
	def upload = apply("upload.png")
	
	/**
	  * @return An icon that represents an sql file
	  */
	def sqlFile = apply("sql.png")
	
	/**
	 * @return Icon used with one-to-one links
	 */
	def oneOnOneLink = apply("double_arrow.png")
	
	/**
	 * @return Icon used with many-to-one links
	 */
	def manyToOneLink = apply("one_to_many_arrow.png")
	
	/**
	 * @return Icon used with one-to-many links
	 */
	def oneToManyLink = manyToOneLink.map { _.flippedHorizontally }
	
	/**
	 * @return Icon used with many-to-many links
	 */
	def manyToManyLink = apply("many_to_many_arrow.png")
	
	
	// OTHER	------------------------------
	
	/**
	 * @param iconName Name of searched icon (including file extension)
	 * @return An icon with specified name
	 */
	def apply(iconName: String) = cache(iconName)
	
	/**
	  * Finds an icon representing specified attribute type
	  * @param attType An attribute type
	  * @return Icon representing specified attribute type
	  */
	def forAttributeType(attType: AttributeType) = attType match
	{
		case ShortStringType => text
		case IntType => numbers
		case DoubleType => decimalNumber
		case BooleanType => checkBox
		case InstantType => time
		case _ => empty // TODO: Add support for other types
	}
	
	/**
	  * Finds an icon representing specified link type
	  * @param linkType A link type
	  * @return An icon for that link type
	  */
	def forLinkType(linkType: LinkTypeCategory) = linkType match
	{
		case ManyToOne => manyToOneLink
		case OneToOne => oneOnOneLink
		case ManyToMany => manyToManyLink
	}
}
