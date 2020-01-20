package dbd.client.vc

import utopia.reflection.localization.LocalString._
import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.model.DisplayedLink
import dbd.core.model.enumeration.LinkTypeCategory.{ManyToMany, ManyToOne, OneToOne}
import utopia.genesis.image.Image
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.{ButtonImageSet, ImageAndTextButton}
import utopia.reflection.util.ComponentContextBuilder

/**
 * Displays a link as a data row
 * @author Mikko Hilpinen
 * @since 20.1.2020, v0.1
 */
class LinkRowVC(initialClassId: Int, initialLink: DisplayedLink, classManager: ClassDisplayManager)
			   (implicit baseCB: ComponentContextBuilder, colorScheme: ColorScheme)
	extends StackableAwtComponentWrapperWrapper with Refreshable[(Int, DisplayedLink)]
{
	// ATTRIBUTES	------------------------
	
	private var _content = initialClassId -> initialLink
	
	private val buttonColor = colorScheme.secondary
	private val linkButton = ImageAndTextButton.contextual(iconForLinkType,
		initialLink.otherClass.name.noLanguageLocalizationSkipped){ () => classManager.openLink(classId,
		displayedLink.otherClass.id) }(baseCB.withColors(buttonColor).result)
	
	
	// COMPUTED	----------------------------
	
	/**
	 * @return The currently displayed link
	 */
	def displayedLink = _content._2
	
	private def classId = _content._1
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = linkButton
	
	override def content_=(newContent: (Int, DisplayedLink)) =
	{
		_content = newContent
		linkButton.images = iconForLinkType
		linkButton.text = displayedLink.otherClass.name.noLanguageLocalizationSkipped
	}
	
	override def content = _content
	
	
	// OTHER	---------------------------
	
	private def iconForLinkType = displayedLink.linkType.category match
	{
		case OneToOne => Icons.oneOnOneLink.forButtonWithBackground(buttonColor)
		case ManyToOne => { if (displayedLink.originClassId == classId) Icons.manyToOneLink else Icons.oneToManyLink
			}.forButtonWithBackground(buttonColor)
		case ManyToMany => Icons.manyToManyLink.forButtonWithBackground(buttonColor)
		case _ => ButtonImageSet.fixed(Image.empty)
	}
}
