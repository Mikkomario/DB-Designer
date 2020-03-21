package dbd.client.main

import utopia.flow.util.CollectionExtensions._
import dbd.client.model.Fonts
import dbd.client.vc.MainVC
import dbd.core.database.ConnectionPool
import dbd.core.util.{Log, ThreadPool}
import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.color.{Color, RGB}
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.mutable.ActorHandler
import utopia.reflection.color.{ColorScheme, ColorSet}
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.{Margins, StackInsets}
import utopia.reflection.text.Font
import utopia.reflection.util.{ComponentContextBuilder, SingleFrameSetup}
import utopia.vault.util.ErrorHandling
import utopia.vault.util.ErrorHandlingPrinciple.Throw

import scala.concurrent.ExecutionContext

/**
 * The main client app for DB Designer
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object DBDesignerClient extends App
{
	GenesisDataType.setup()
	ErrorHandling.defaultPrinciple = Throw // Will throw errors during development
	
	// Sets up required context
	implicit val defaultLanguageCode: String = "EN"
	implicit val localizer: Localizer = NoLocalization
	
	val actorHandler = ActorHandler()
	
	val primaryColors = ColorSet(RGB.grayWithValue(40), RGB.grayWithValue(72), Color.black)
	val secondaryColors = ColorSet(RGB.withValues(255, 145, 0), RGB.withValues(255, 194, 70), RGB.withValues(197, 98, 0))
	implicit val colorScheme: ColorScheme = ColorScheme(primaryColors, secondaryColors)
	
	val baseFont = Font("Roboto Condensed", 12, scaling = 2.0)
	implicit val fonts: Fonts = Fonts(baseFont)
	
	implicit val margins: Margins = Margins(16)
	
	implicit val baseCB: ComponentContextBuilder = ComponentContextBuilder(actorHandler, baseFont, secondaryColors.light,
		secondaryColors.light, 320, insets = StackInsets.symmetric(margins.verySmall.any), stackMargin = margins.medium.downscaling,
		relatedItemsStackMargin = Some(margins.small.downscaling))
	
	implicit val exc: ExecutionContext = ThreadPool.executionContext
	
	
	// Reads displayed data from DB
	ConnectionPool.tryWith { implicit connection =>
		val content = new MainVC
		new SingleFrameSetup(actorHandler, Frame.windowed(content, "DB Designer", Program)).start()
	}.failure.foreach { Log(_, "Failed to run DB Designer client") }
}
