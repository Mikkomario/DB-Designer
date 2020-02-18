package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.{DeleteQuestionDialog, EditLinkDialog}
import dbd.client.model.DisplayedLink
import dbd.core.model.enumeration.LinkTypeCategory.{ManyToMany, ManyToOne, OneToOne}
import dbd.core.model.existing.Class
import utopia.genesis.image.Image
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.{ButtonImageSet, ImageAndTextButton, ImageButton}
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.LocalString._
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Displays a link as a data row
 * @author Mikko Hilpinen
 * @since 20.1.2020, v0.1
 */
// TODO: Get rid of passing default language code as a parameter to dialog
class LinkRowVC(initialClass: Class, initialLink: DisplayedLink, classManager: ClassDisplayManager)
			   (implicit baseCB: ComponentContextBuilder, colorScheme: ColorScheme, margins: Margins,
				defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[(Class, DisplayedLink)]
{
	// ATTRIBUTES	------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private var _content = initialClass -> initialLink
	
	private val buttonColor = colorScheme.primary
	private val linkButton = ImageAndTextButton.contextual(iconForLinkType,
		initialLink.displayName.noLanguageLocalizationSkipped){ () => classManager.openLink(classId,
		displayedLink.otherClass.id) }(baseCB.withColors(buttonColor).result)
	private val view = Stack.buildRowWithContext(isRelated = true) { row =>
		row += linkButton
		// Adds link edit button
		row += ImageButton.contextual(Icons.edit.forButtonWithoutText(colorScheme.secondary)) { () =>
			parentWindow.foreach { window =>
				val linkToEdit = displayedLink
				new EditLinkDialog(Some(linkToEdit.configuration), displayedClass, classManager.linkableClasses(classId))
					.display(window).foreach { _.foreach { newLinkVersion => classManager.editLink(linkToEdit.link, newLinkVersion)
				} } }
		}
		// Adds delete link button
		row += ImageButton.contextual(Icons.close.forButtonWithoutText(colorScheme.secondary)) { () =>
			parentWindow.foreach { window =>
				val linkToDelete = displayedLink
				DeleteQuestionDialog.forLink(linkToDelete.displayName).display(window).foreach {
					if (_) classManager.deleteLink(linkToDelete.link) }
			}
		}
	}
	
	
	// COMPUTED	----------------------------
	
	/**
	 * @return The currently displayed link
	 */
	def displayedLink = _content._2
	
	private def displayedClass = _content._1
	
	private def classId = displayedClass.id
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: (Class, DisplayedLink)) =
	{
		_content = newContent
		linkButton.images = iconForLinkType
		linkButton.text = displayedLink.displayName.noLanguageLocalizationSkipped
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
