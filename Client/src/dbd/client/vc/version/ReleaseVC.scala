package dbd.client.vc.version

import java.time.Instant

import dbd.client.controller.Icons
import dbd.client.dialog.ExportSqlDialog
import dbd.client.model.{ChangedItems, DisplayedRelease}
import dbd.client.vc.SeparatorLine
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.{ColorScheme, ComponentColor}
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.{ImageAndTextButton, ImageButton}
import utopia.reflection.component.swing.label.{ItemLabel, TextLabel}
import utopia.reflection.container.stack.StackLayout.Leading
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.{Stack, SwitchPanel}
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.LocalString._
import utopia.reflection.localization.{DisplayFunction, LocalizedString, Localizer}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

object ReleaseVC
{
	private implicit val language: String = "en"
}

/**
  * Displays data of a single release (published or upcoming)
  * @author Mikko Hilpinen
  * @since 2.2.2020, v0.1
  */
class ReleaseVC(initialRelease: DisplayedRelease, backgroundColor: ComponentColor)(onUploadButtonPressed: () => Unit)
			   (implicit baseCB: ComponentContextBuilder, localizer: Localizer, margins: Margins,
				colorScheme: ColorScheme, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedRelease]
{
	import ReleaseVC._
	
	// ATTRIBUTES	------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.withTextColor(backgroundColor.defaultTextColor).result
	
	private var _content = initialRelease
	
	private val releaseNameLabel = TextLabel.contextual(initialRelease.release.map {
		_.versionNumber.toString.noLanguageLocalizationSkipped }.getOrElse("Unreleased"))
	private val releaseTimeLabel = ItemLabel.contextual(initialRelease.release.map { _.released }.getOrElse(Instant.now()),
		DisplayFunction.ddmmyyyy)
	private val changesStack = Stack.column[ChangeListVC](margin = 0.fixed, layout = Leading)
	
	private val changeManager = new ContainerContentManager[
		(LocalizedString, ChangedItems, Boolean), Stack[ChangeListVC], ChangeListVC](changesStack)({
		case (title, list, isExpanded) => new ChangeListVC(list, isExpanded, title, 6, backgroundColor) })
	
	private val uploadButtonColor = colorScheme.secondary.dark
	private val uploadButton = ImageAndTextButton.contextual(Icons.upload.forButtonWithBackground(uploadButtonColor),
		"Upload")(onUploadButtonPressed)(baseCB.withColors(uploadButtonColor).result)
	private val exportSqlButton = ImageButton.contextual(Icons.sqlFile.forButtonWithoutText(uploadButtonColor)) { () =>
		_content.release.foreach { release => parentWindow.foreach { window => new ExportSqlDialog(release).display(window) } }
	}
	val buttonView = SwitchPanel[AwtStackable](exportSqlButton)
	
	private val view = Stack.buildColumnWithContext() { stack =>
		stack += Stack.buildRowWithContext(isRelated = true) { mainRow =>
			mainRow += releaseNameLabel
			mainRow += releaseTimeLabel
			mainRow += SeparatorLine()
			mainRow += buttonView
		}
		stack += changesStack.alignedToSide(Direction2D.Left, useLowPriorityLength = true)
	}
	
	
	// INITIAL CODE	------------------------
	
	updateView()
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: DisplayedRelease) =
	{
		if (_content != newContent)
		{
			_content = newContent
			updateView()
		}
	}
	
	override def content = _content
	
	
	// OTHER	----------------------------
	
	private def updateView() =
	{
		releaseNameLabel.text = _content.release.map {
			_.versionNumber.toString.noLanguageLocalizationSkipped }.getOrElse("Unreleased")
		_content.release.foreach { r => releaseTimeLabel.content = r.released }
		releaseTimeLabel.isVisible = _content.release.isDefined
		val changes = Vector("- Added:" -> _content.added, "- Modified:" -> _content.modified, "- Removed:" -> _content.removed)
			.filter { _._2.nonEmpty }
		// TODO: Handle expansion (currently all default to false)
		changeManager.content = changes.map { case (title, c) => (title.autoLocalized, c, false) }
		
		// Displays either upload or export button
		if (_content.release.isDefined)
			buttonView.set(exportSqlButton)
		else
			buttonView.set(uploadButton)
	}
}
