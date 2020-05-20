package dbd.api.database.factory.user

import dbd.api.database.Tables
import dbd.api.database.model.user.UserSettingsModel
import dbd.core.model.existing.user
import dbd.core.model.partial.user.UserSettingsData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.nosql.factory.FromValidatedRowModelFactory

object UserSettingsFactory extends FromValidatedRowModelFactory[user.UserSettings]
{
	// IMPLEMENTED	----------------------------------
	
	override def table = Tables.userSettings
	
	override protected def fromValidatedModel(model: Model[Constant]) = user.UserSettings(model("id").getInt,
		model(UserSettingsModel.userIdAttName).getInt, UserSettingsData(model("name").getString, model("email").getString))
}


