package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.mysql.model.VersionNumber
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.component.swing.{FilterDocument, TextField}
import utopia.reflection.text.Regex
import utopia.reflection.shape.LengthExtensions._

object PublishReleaseDialog
{
	private implicit val language: String = "en"
	
	private val versionNumberRegex = Regex("v").noneOrOnce + Regex.digit.oneOrMoreTimes +
		(Regex("\\.") + Regex.digit.oneOrMoreTimes).withinParenthesis.zeroOrMoreTimes
	private val versionNumberFilter = Regex("[v.]") || Regex.digit
}

/**
  * Used for publishing new releases
  * @author Mikko Hilpinen
  * @since 20.2.2020, v0.1
  */
class PublishReleaseDialog(latestVersionNumber: Option[VersionNumber]) extends InputDialog[Option[VersionNumber]]
{
	import PublishReleaseDialog._
	import dbd.client.view.DefaultContext._
	
	// ATTRIBUTES	----------------------------
	
	private val textContext = baseContext.inContextWithBackground(dialogBackground).forTextComponents()
	
	private val numberField = textContext.forGrayFields.use { implicit c =>
		TextField.contextual(standardInputWidth.downscaling, FilterDocument(versionNumberFilter),
			latestVersionNumber.map { _.next(1).toString }.getOrElse("v1"), Some("Eg. v1.2.3"),
			Some(versionNumberRegex))
	}
	
	// IMPLEMENTED	----------------------------
	
	override protected def fields =
	{
		val inputRow = InputRowInfo("Version Number", numberField)
		latestVersionNumber match
		{
			case Some(latest) =>
				val latestLabel = textContext.use { implicit c => TextLabel.contextual(latest.toString.noLanguageLocalizationSkipped) }
				Vector(inputRow, InputRowInfo("Latest", latestLabel))
			case None => Vector(inputRow)
		}
	}
	
	override protected def produceResult = Right(numberField.value.map(VersionNumber.parse))
	
	override protected def defaultResult = None
	
	override protected def title = "Publish New Version"
}
