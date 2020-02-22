package dbd.client.vc.version

import utopia.flow.util.CollectionExtensions._
import dbd.client.controller.ReadReleaseData
import dbd.client.dialog.PublishReleaseDialog
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.DisplayedRelease
import dbd.core.database.ConnectionPool
import dbd.core.util.Log
import dbd.mysql.controller.GenerateTableStructure
import utopia.genesis.shape.shape2D.Direction2D.Up
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.ComponentContextBuilder

import scala.concurrent.ExecutionContext

/**
  * Used for interacting with database releases
  * @author Mikko Hilpinen
  * @since 22.2.2020, v0.1
  */
class ReleasesVC(initialDatabaseId: Int)
				(implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme,
				 localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Int]
{
	// ATTRIBUTES	-------------------------
	
	private var databaseId = initialDatabaseId
	
	private val backgroundColor = colorScheme.primary.light
	private val releasesStack = Stack.column[ReleaseVC](margins.small.any)
	private val releaseManager = new ContainerContentManager[DisplayedRelease, Stack[ReleaseVC], ReleaseVC](
		releasesStack)(r => new ReleaseVC(r, backgroundColor)({ () => onUploadPressed() }))
	
	private val view = releasesStack.alignedToSide(Up, useLowPriorityLength = true)
		.framed(margins.medium.any.square, backgroundColor)
	
	
	// INITIAL CODE	-------------------------
	
	updateData()
	
	
	// IMPLEMENTED	-------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Int) =
	{
		if (databaseId != newContent)
		{
			databaseId = newContent
			updateData()
		}
	}
	
	override def content = databaseId
	
	
	// OTHER	-----------------------------
	
	private def updateData() =
	{
		ConnectionPool.tryWith { implicit connection =>
			// Reads release data from DB
			val releases = ReadReleaseData.forDatabaseWithId(databaseId)
			// Updates displays
			releaseManager.content = releases
		}.failure.foreach { Log(_, "Failed to update ReleasesVC") }
	}
	
	private def onUploadPressed(): Unit =
	{
		// Displays a dialog that requests the new version number, publishes a new release if user wants it
		parentWindow.foreach { window =>
			val targetedDatabaseId = databaseId
			new PublishReleaseDialog(releaseManager.content.findMap { _.release }.map { _.versionNumber })
				.display(window).foreach { _.foreach { newVersionNumber =>
					ConnectionPool.tryWith { implicit connection =>
						GenerateTableStructure(targetedDatabaseId, newVersionNumber)
					}.failure.foreach { Log(_, "Failed to upload a new release") }
				} }
		}
		updateData()
	}
}
