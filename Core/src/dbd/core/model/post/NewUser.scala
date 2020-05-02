package dbd.core.model.post

import dbd.core.model.partial.UserSettingsData
import utopia.flow.generic.ValueConversions._
import utopia.flow.datastructure.immutable.{Model, ModelDeclaration, ModelValidationFailedException, Value}
import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property
import utopia.flow.generic.{FromModelFactory, ModelConvertible, ModelType, StringType, VectorType}

import scala.util.{Failure, Success}

object NewUser extends FromModelFactory[NewUser]
{
	private val schema = ModelDeclaration("settings" -> ModelType, "password" -> StringType,
		"language_ids" -> VectorType)
	
	override def apply(model: template.Model[Property]) = schema.validate(model).toTry.flatMap { valid =>
		UserSettingsData(valid("settings").getModel).flatMap { settings =>
			// Either device id or device name must be provided
			val deviceId = valid("device_id").int
			val deviceName = valid("device_name").string.filterNot { _.isEmpty }
			if (deviceId.isEmpty && deviceName.isEmpty)
				Failure(new ModelValidationFailedException("Either device_id or device_name must be provided"))
			else
			{
				val deviceIdOrName = deviceId match
				{
					case Some(id) => Right(id)
					case None => Left(deviceName.get)
				}
				Success(NewUser(settings, valid("password").getString,
					valid("language_ids").getVector.flatMap { _.int }, deviceIdOrName))
			}
		}
	}
}

/**
  * A model used when creating new users from client side
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param settings Initial user settings
  * @param password Initial user password
  * @param languageIds List of ids of the languages known by the user
  * @param device Either Right: Existing device id or Left: Device name
  */
case class NewUser(settings: UserSettingsData, password: String, languageIds: Vector[Int], device: Either[String, Int])
	extends ModelConvertible
{
	override def toModel =
	{
		val deviceData: (String, Value) = device match
		{
			case Right(deviceId) => "device_id" -> deviceId
			case Left(deviceName) => "device_name" -> deviceName
		}
		Model(Vector("settings" -> settings.toModel, "password" -> password, "language_ids" -> languageIds, deviceData))
	}
}
