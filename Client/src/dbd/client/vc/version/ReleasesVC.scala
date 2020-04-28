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
import utopia.reflection.component.Refreshable
import utopia.reflection.component.context.ColorContext
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContainerContentManager

/**
  * Used for interacting with database releases
  * @author Mikko Hilpinen
  * @since 22.2.2020, v0.1
  */
class ReleasesVC(initialDatabaseId: Int) extends StackableAwtComponentWrapperWrapper with Refreshable[Int]
{
	import dbd.client.view.DefaultContext._
	
	// ATTRIBUTES	-------------------------
	
	private var databaseId = initialDatabaseId
	
	private implicit val context: ColorContext = baseContext.inContextWithBackground(colorScheme.primary.light)
	private val releasesStack = Stack.column[ReleaseVC](margins.medium.downscaling)
	private val releaseManager = ContainerContentManager.forImmutableStates[DisplayedRelease, ReleaseVC](releasesStack) {
		(a, b) => a.release.map { _.id } == b.release.map { _.id } } { r => new ReleaseVC(r)({ onUploadPressed() }) }
	
	private val view = releasesStack.alignedToSide(Up, useLowPriorityLength = true)
		.framed(margins.medium.any.square, context.containerBackground)
	
	
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
			
				// Once data has been uploaded, updates view
				updateData()
			} }
		}
	}
}
