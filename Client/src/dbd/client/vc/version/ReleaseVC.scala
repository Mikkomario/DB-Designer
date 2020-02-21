package dbd.client.vc.version

import java.awt.Desktop
import java.nio.file.Path
import java.time.Instant

import utopia.flow.util.CollectionExtensions._
import dbd.core.model.enumeration.NamingConvention._
import utopia.flow.util.FileExtensions._
import dbd.client.controller.Icons
import dbd.client.model.{ChangedItems, DisplayedRelease}
import dbd.client.vc.SeparatorLine
import dbd.core.database.{ConnectionPool, Database}
import dbd.core.util.Log
import dbd.mysql.controller.SqlWriter
import dbd.mysql.database.Release
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
import scala.util.{Failure, Success}

object ReleaseVC
{
	private implicit val language: String = "en"
	
	private val exportDirectory: Path = "export/sql"
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
	private val changesStack = Stack.column[ChangeListVC](margin = margins.medium.downscaling, layout = Leading)
	
	private val changeManager = new ContainerContentManager[
		(LocalizedString, ChangedItems, Boolean), Stack[ChangeListVC], ChangeListVC](changesStack)({
		case (title, list, isExpanded) => new ChangeListVC(list, isExpanded, title, 6, backgroundColor) })
	
	private val uploadButtonColor = colorScheme.secondary.dark
	private val uploadButton = ImageAndTextButton.contextual(Icons.upload.forButtonWithBackground(uploadButtonColor),
		"Upload")(onUploadButtonPressed)(baseCB.withColors(uploadButtonColor).result)
	private val exportSqlButton = ImageButton.contextual(Icons.sqlFile.forButtonWithoutText(uploadButtonColor)) { () =>
		_content.release.foreach { release =>
			ConnectionPool.tryWith { implicit connection =>
				Database(release.databaseId).configuration.during(release.released).get.foreach { dbConfiguration =>
					// Checks whether there already exists an sql file
					val databaseName = dbConfiguration.name.toUnderscore
					val exportFilePath = exportDirectory/databaseName/s"$databaseName-${release.versionNumber}.sql"
					val pathWithData =
					{
						if (exportFilePath.exists)
							Success(exportFilePath)
						else
						{
							// Exports sql file if necessary
							exportFilePath.createParentDirectories().flatMap { _ =>
								val tables = Release(release.id).tables
								val sql = SqlWriter(databaseName, tables)
								exportFilePath.write(sql)
							}
						}
					}
					
					// Opens the file in desktop
					pathWithData match
					{
						case Success(filePath) =>
							// TODO: Handle exceptions and if desktop is not supported, show dialog
							if (Desktop.isDesktopSupported)
								Desktop.getDesktop.open(filePath.toFile)
						case Failure(error) =>
							// TODO: Show error
							Log(error, "Failed to export table structure to sql")
					}
				}
			}.failure.foreach { Log(_, "Export to SQL failed") }
		}
	}
	val buttonView = SwitchPanel[AwtStackable](exportSqlButton)
	
	private val view = Stack.buildColumnWithContext() { stack =>
		stack += Stack.buildRowWithContext(isRelated = true) { mainRow =>
			mainRow += releaseNameLabel
			mainRow += releaseTimeLabel
			mainRow += SeparatorLine()
			mainRow += buttonView
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
