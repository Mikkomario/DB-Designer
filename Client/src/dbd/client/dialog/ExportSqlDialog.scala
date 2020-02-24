package dbd.client.dialog

import java.nio.file.Path
import java.time.format.DateTimeFormatter

import scala.language.existentials
import dbd.mysql.database
import utopia.flow.util.CollectionExtensions._
import dbd.core.model.enumeration.NamingConvention._
import dbd.core.database.{ConnectionPool, Database}
import dbd.core.model.error.NoDataFoundException
import dbd.core.util.Log
import dbd.mysql.controller.SqlWriter
import dbd.mysql.database.Releases
import dbd.mysql.model.change.ReleasesComparison
import utopia.flow.util.TimeExtensions._
import utopia.flow.util.FileExtensions._
import dbd.mysql.model.existing.Release
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.{DropDown, TabSelection}
import utopia.reflection.localization.{DisplayFunction, LocalString, LocalizedString, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.collection.immutable.VectorBuilder
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

object ExportSqlDialog
{
	private implicit val langauge: String = "en"
	
	private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
	private val exportDirectory: Path = "export/sql"
	
	private sealed trait ExportMode
	{
		val name: LocalString
	}
	
	private object ExportMode
	{
		case object All extends ExportMode
		{
			override val name = "Current Structure"
		}
		case object Changes extends ExportMode
		{
			override val name = "Changes Since"
		}
		
		val values = Vector(All, Changes)
	}
	
	private sealed trait FileAction
	{
		val name: LocalString
		def apply(path: Path): Try[Unit]
	}
	
	private object FileAction
	{
		case object Open extends FileAction
		{
			override val name = "Open Generated File"
			
			def apply(path: Path) = path.openInDesktop()
		}
		case object ShowFileLocation extends FileAction
		{
			override val name = "Show File Location"
			
			def apply(path: Path) = path.openDirectory()
		}
		
		val values = Vector(Open, ShowFileLocation)
	}
}

/**
  * Used for exporting table structure in SQL format
  * @author Mikko Hilpinen
  * @since 23.2.2020, v0.1
  */
class ExportSqlDialog(release: Release)
					 (implicit baseCB: ComponentContextBuilder, colorScheme: ColorScheme, margins: Margins,
					  localizer: Localizer, exc: ExecutionContext) extends InputDialog[Unit]
{
	import ExportSqlDialog._
	import ExportSqlDialog.ExportMode._
	import ExportSqlDialog.FileAction._
	
	// ATTRIBUTES	-----------------------
	
	private val previousReleases = ConnectionPool.tryWith { implicit connection =>
		Releases.forDatabaseWithId(release.databaseId).before(release.released, 25)
	} match
	{
		case Success(releases) => releases
		case Failure(error) =>
			Log(error, "Failed to read releases data")
			Vector()
	}
	
	private implicit val baseContext: ComponentContext = baseCB.result
	private val changeOptionIsAvailable = previousReleases.nonEmpty
	private val changeVisibilityPointer =
	{
		if (changeOptionIsAvailable)
			Some(new PointerWithEvents(true))
		else
			None
	}
	
	private val selectModeTab =
	{
		if (changeOptionIsAvailable)
		{
			val tab = TabSelection.contextual[ExportMode](DisplayFunction.localized[ExportMode] { _.name }, ExportMode.values,
				margins.small)(baseCB.withColors(colorScheme.primary).withHighlightColor(
				colorScheme.secondary.forBackground(colorScheme.primary)).result)
			tab.selectOne(Changes)
			Some(tab)
		}
		else
			None
	}
	private val baseVersionSelectDD =
	{
		if (changeOptionIsAvailable)
		{
			val dd = DropDown.contextual[Release]("Select Base Version",
				DisplayFunction.noLocalization[Release] { r =>
					s"${r.versionNumber.toString} (${r.released.toStringWith(dateFormatter)})" }, previousReleases)
			dd.selectOne(previousReleases.head)
			Some(dd)
		}
		else
			None
	}
	private val actionDD = DropDown.contextual[FileAction]("Select Action",
		DisplayFunction.localized[FileAction] { _.name }, FileAction.values)
	
	
	// INITIAL CODE	-----------------------
	
	actionDD.selectOne(Open)
	// Selected mode affects version select visibility (if available)
	selectModeTab.foreach { tab => changeVisibilityPointer.foreach { pointer => tab.addValueListener { e =>
		pointer.value = e.newValue.contains(Changes) } } }
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def fields =
	{
		val buffer = new VectorBuilder[InputRowInfo]
		selectModeTab.foreach { f => buffer += InputRowInfo("Mode", f) }
		baseVersionSelectDD.foreach { f => buffer += InputRowInfo("Since Version", f,
			rowVisibilityPointer = changeVisibilityPointer) }
		buffer += InputRowInfo("Action", actionDD)
		
		buffer.result()
	}
	
	override protected def produceResult =
	{
		val selectedMode = selectModeTab.flatMap { _.value }.getOrElse(All)
		val sourceVersion = if (selectedMode == Changes) baseVersionSelectDD.flatMap { _.value } else None
		
		val generatedPath = ConnectionPool.tryWith { implicit connection =>
			val dbConfigAccess = Database(release.databaseId).configuration
			dbConfigAccess.during(release.released).get.orElse(dbConfigAccess.get).map { dbConfiguration =>
				// Generates file name & path
				val databaseName = dbConfiguration.name.toUnderscore
				val exportDirectoryForDB = exportDirectory/databaseName
				val exportFilePath = sourceVersion match
				{
					case Some(source) => exportDirectoryForDB/s"$databaseName-${source.versionNumber}-to-${release.versionNumber}.sql"
					case None => exportDirectoryForDB/s"$databaseName-${release.versionNumber}.sql"
				}
				// Checks whether there already exists an sql file
				if (exportFilePath.exists)
					Success(exportFilePath)
				else
				{
					// Exports sql file
					exportFilePath.createParentDirectories().flatMap { _ =>
						val tables = database.Release(release.id).tables
						val sql = sourceVersion match
						{
							case Some(source) =>
								val oldTables = database.Release(source.id).tables
								ReleasesComparison(release.databaseId, oldTables, tables).toSql(databaseName,
									source.versionNumber.toString, release.versionNumber.toString)
							case None => SqlWriter(databaseName, tables)
						}
						//SqlWriter(databaseName, tables)
						exportFilePath.write(sql)
					}
				}
			}.toTry(new NoDataFoundException(s"No configuration found for database ${release.databaseId}")).flatten
		}.flatten
		
		generatedPath.flatMap { p =>
			actionDD.value.getOrElse(Open) match
			{
				case Open => p.openInDesktop()
				case ShowFileLocation => p.openDirectory()
			}
		}.failure.foreach { Log(_, s"Sql export failed for release: ${release.versionNumber}") }
		
		Right(Unit)
	}
	
	override protected def defaultResult = Unit
	
	override protected def title = ("Export %s": LocalizedString).interpolate(release.versionNumber.toString)
}
