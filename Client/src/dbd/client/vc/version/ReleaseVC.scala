package dbd.client.vc.version

import java.time.Instant

import dbd.client.model.{ChangedItems, DisplayedRelease}
import dbd.client.vc.SeparatorLine
import utopia.reflection.color.ComponentColor
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.{ItemLabel, TextLabel}
import utopia.reflection.container.stack.StackLayout.Leading
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.LocalString._
import utopia.reflection.localization.{DisplayFunction, LocalizedString, Localizer}
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

/**
  * Displays data of a single release (published or upcoming)
  * @author Mikko Hilpinen
  * @since 2.2.2020, v0.1
  */
class ReleaseVC(initialRelease: DisplayedRelease, backgroundColor: ComponentColor)
			   (implicit baseCB: ComponentContextBuilder, localizer: Localizer, margins: Margins)
	extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedRelease]
{
	// ATTRIBUTES	------------------------
	
	private implicit val language: String = "en"
	private implicit val baseContext: ComponentContext = baseCB.withTextColor(backgroundColor.defaultTextColor).result
	
	private var _content = initialRelease
	
	private val releaseNameLabel = TextLabel.contextual(initialRelease.release.map {
		_.versionNumber.toString.noLanguageLocalizationSkipped }.getOrElse("Unreleased"))
	private val releaseTimeLabel = ItemLabel.contextual(initialRelease.release.map { _.released }.getOrElse(Instant.now()),
		DisplayFunction.ddmmyyyy)
	private val changesStack = Stack.column[ChangeListVC](margin = margins.medium.downscaling, layout = Leading)
	
	private val changeManager = new ContainerContentManager[
		(LocalizedString, ChangedItems, Boolean), Stack[ChangeListVC], ChangeListVC](changesStack)({
		case (title, list, isExpanded) => new ChangeListVC(list, isExpanded, title, 6, backgroundColor) })
	
	private val view = Stack.buildColumnWithContext() { stack =>
		stack += Stack.buildRowWithContext(isRelated = true) { mainRow =>
			mainRow += releaseNameLabel
			mainRow += releaseTimeLabel
			mainRow += SeparatorLine()
			// TODO: Add buttons
		}
		stack += changesStack
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
		val changes = Vector("Added" -> _content.added, "Modified" -> _content.modified, "Removed" -> _content.removed)
			.filter { _._2.nonEmpty }
		changeManager.content = changes.map { case (title, c) => (title.autoLocalized, c, true) }
	}
}
