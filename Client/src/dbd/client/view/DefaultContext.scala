package dbd.client.view

import dbd.client.model.Fonts
import dbd.core.util.ThreadPool
import utopia.genesis.color.{Color, RGB}
import utopia.genesis.handling.mutable.ActorHandler
import utopia.reflection.color.{ColorScheme, ColorSet}
import utopia.reflection.component.context.{AnimationContext, BaseContext}
import utopia.reflection.localization.{Localizer, NoLocalization}
import utopia.reflection.shape.Margins
import utopia.reflection.text.Font
import utopia.reflection.shape.LengthExtensions._

import scala.concurrent.ExecutionContext

/**
  * Contains various default settings used in the view and VC level
  * @author Mikko Hilpinen
  * @since 28.4.2020, v1
  */
object DefaultContext
{
	implicit val localizer: Localizer = NoLocalization
	
	val actorHandler = ActorHandler()
	
	val primaryColors = ColorSet(RGB.grayWithValue(40), RGB.grayWithValue(72), Color.black)
	val secondaryColors = ColorSet(RGB.withValues(255, 145, 0), RGB.withValues(255, 194, 70), RGB.withValues(197, 98, 0))
	val colorScheme: ColorScheme = ColorScheme(primaryColors, secondaryColors)
	
	val standardInputWidth = 320
	val switchWidth = 64.any
	val margins: Margins = Margins(16)
	
	val baseFont = Font("Roboto Condensed", 12, scaling = 2.0)
	implicit val fonts: Fonts = Fonts(baseFont)
	
	val baseContext = BaseContext(actorHandler, baseFont, colorScheme, margins)
	implicit val animationContext: AnimationContext = AnimationContext(actorHandler)
	
	implicit def exc: ExecutionContext = ThreadPool.executionContext
}
