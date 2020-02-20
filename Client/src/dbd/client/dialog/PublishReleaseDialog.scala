package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.mysql.model.VersionNumber
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.component.swing.{FilterDocument, TextField}
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.text.Regex
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

object PublishReleaseDialog
{
	private implicit val language: String = "en"
	
	private val versionNumberRegex = Regex("v").noneOrOnce + Regex.digit.oneOrMoreTimes +
		(Regex("\\.") + Regex.digit.oneOrMoreTimes).zeroOrMoreTimes
	private val versionNumberFilter = Regex("[v.]") || Regex.digit
}

/**
  * Used for publishing new releases
  * @author Mikko Hilpinen
  * @since 20.2.2020, v0.1
  */
class PublishReleaseDialog(latestVersionNumber: Option[VersionNumber])
						  (implicit colorScheme: ColorScheme, baseCB: ComponentContextBuilder, margins: Margins,
						   exc: ExecutionContext, localizer: Localizer) extends InputDialog[Option[VersionNumber]]
{
	import PublishReleaseDialog._
	
	// ATTRIBUTES	----------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val numberField = TextField.contextual(FilterDocument(versionNumberFilter),
		latestVersionNumber.map { _.next(1).toString }.getOrElse("v1"),
		Some("Eg. v1.2.3"), Some(versionNumberRegex))
	
	
	// IMPLEMENTED	----------------------------
	
	override protected def fields =
	{
		val inputRow = InputRowInfo("Version Number", numberField)
		latestVersionNumber match
		{
			case Some(latest) => Vector(inputRow, InputRowInfo("Latest",
				TextLabel.contextual(latest.toString.noLanguageLocalizationSkipped)))
			case None => Vector(inputRow)
		}
	}
	
	override protected def produceResult = Right(numberField.value.map(VersionNumber.parse))
	
	override protected def defaultResult = None
	
	override protected def title = "Publish New Version"
}
