package dbd.client.main

import dbd.api.database.ConnectionPool
import utopia.flow.util.CollectionExtensions._
import dbd.client.vc.MainVC
import dbd.core.util.Log
import utopia.genesis.generic.GenesisDataType
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.shape.Alignment.Top
import utopia.reflection.util.SingleFrameSetup
import utopia.vault.util.ErrorHandling
import utopia.vault.util.ErrorHandlingPrinciple.Throw

/**
 * The main client app for DB Designer
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object DBDesignerClient extends App
{
	GenesisDataType.setup()
	ErrorHandling.defaultPrinciple = Throw // Will throw errors during development
	
	import dbd.client.view.DefaultContext._
	private implicit val languageCode: String = "en"
	
	// Reads displayed data from DB
	ConnectionPool.tryWith { implicit connection =>
		val content = new MainVC
		new SingleFrameSetup(actorHandler, Frame.windowed(content, "DB Designer", Program, Top)).start()
	}.failure.foreach { Log(_, "Failed to run DB Designer client") }
}
