package dbd.client.vc.version

import java.time.Instant

import dbd.client.controller.Icons
import dbd.client.dialog.ExportSqlDialog
import dbd.client.model.{ChangedItems, DisplayedRelease}
import dbd.client.vc.SeparatorLine
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.Refreshable
import utopia.reflection.component.context.ColorContext
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.{ImageAndTextButton, ImageButton}
import utopia.reflection.component.swing.label.{ItemLabel, TextLabel}
import utopia.reflection.container.stack.StackLayout.Leading
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.{Stack, SwitchPanel}
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.LocalString._
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.LengthExtensions._

object ReleaseVC
{
	private implicit val language: String = "en"
}

/**
  * Displays data of a single release (published or upcoming)
  * @author Mikko Hilpinen
  * @since 2.2.2020, v0.1
  */
class ReleaseVC(initialRelease: DisplayedRelease)(onUploadButtonPressed: => Unit)(implicit val parentContext: ColorContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedRelease]
{
	import ReleaseVC._
	import dbd.client.view.DefaultContext._
	
	// ATTRIBUTES	------------------------
	
	private val textContext = parentContext.forTextComponents()
	
	private var _content = initialRelease
	
	private val releaseNameLabel = TextLabel.contextual(initialRelease.release.map {
		_.versionNumber.toString.noLanguageLocalizationSkipped }.getOrElse("Unreleased"))(textContext)
	private val releaseTimeLabel = ItemLabel.contextual(initialRelease.release.map { _.released }.getOrElse(Instant.now()),
		DisplayFunction.ddmmyyyy)(textContext)
	private val changesStack = Stack.column[ChangeListVC](margin = 0.fixed, layout = Leading)
	private val changeManager = ContainerContentManager.forImmutableStates[(LocalizedString, ChangedItems, Boolean),
		ChangeListVC](changesStack) { (a, b) => a._1 == b._1 && a._2 == b._2 } { case (title, list, isExpanded) =>
		new ChangeListVC(list, isExpanded, title, 6) }
	
	private val uploadButton = textContext.forSecondaryColorButtons.use { implicit btnC =>
		ImageAndTextButton.contextual(Icons.upload.inButton, "Upload")(onUploadButtonPressed) }
	private val exportSqlButton = ImageButton.contextual(Icons.sqlFile.asIndividualButton) {
		_content.release.foreach { release => parentWindow.foreach { window => new ExportSqlDialog(release).display(window) } }
	}
	val buttonView = SwitchPanel[AwtStackable](exportSqlButton)
	
	private val view = Stack.buildColumnWithContext() { stack =>
		stack += Stack.buildRowWithContext(isRelated = true) { mainRow =>
			mainRow += releaseNameLabel
			mainRow += releaseTimeLabel
			mainRow += SeparatorLine()(textContext)
			mainRow += buttonView
		}
		stack += changesStack.alignedToSide(Direction2D.Left)
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
