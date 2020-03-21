package dbd.client.test

import dbd.client.controller.ReadReleaseData
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.{DisplayedRelease, Fonts}
import dbd.client.vc.version.ReleaseVC
import dbd.core.database.ConnectionPool
import dbd.core.util.ThreadPool
import utopia.genesis.color.{Color, RGB}
import utopia.genesis.generic.GenesisDataType
import utopia.genesis.handling.mutable.ActorHandler
import utopia.reflection.color.{ColorScheme, ColorSet}
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.window.Frame
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.{Margins, StackInsets}
import utopia.reflection.text.Font
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder, SingleFrameSetup}
import utopia.vault.util.ErrorHandling
import utopia.vault.util.ErrorHandlingPrinciple.Throw

import scala.concurrent.ExecutionContext

/**
  * A simple test for displaying version data
  * @author Mikko Hilpinen
  * @since 18.2.2020, v0.1
  */
object VersionViewTest extends App
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
	
	implicit val baseCB: ComponentContextBuilder = ComponentContextBuilder(actorHandler, baseFont, secondaryColors,
		secondaryColors.light, 320, insets = StackInsets.symmetric(margins.small.any), stackMargin = margins.medium.downscaling,
		relatedItemsStackMargin = Some(margins.small.downscaling))
	implicit val baseContext: ComponentContext = baseCB.result
	
	implicit val exc: ExecutionContext = ThreadPool.executionContext
	ConnectionPool { implicit connection =>
		// Reads release data
		val releases = ReadReleaseData.forDatabaseWithId(1)
		
		val releasesStack = Stack.column[ReleaseVC](margins.small.any)
		val releaseManager = new ContainerContentManager[DisplayedRelease, Stack[ReleaseVC], ReleaseVC](
			releasesStack)(r => new ReleaseVC(r, primaryColors)({ () => uploadButtonPressed() }))
		releaseManager.content = releases
		
		val view = releasesStack.framed(margins.medium.any x margins.medium.any, primaryColors)
		
		new SingleFrameSetup(actorHandler, Frame.windowed(view, "DB Designer", Program)).start()
	}
	
	private def uploadButtonPressed() =
	{
		println("Upload button pressed")
	}
}
