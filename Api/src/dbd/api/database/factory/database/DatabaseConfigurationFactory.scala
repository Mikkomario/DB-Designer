package dbd.api.database.factory.database

import dbd.api.database.Tables
import dbd.core.model.existing.database
import dbd.core.model.existing.database.DatabaseConfiguration
import dbd.core.model.partial.database.DatabaseConfigurationData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.{Deprecatable, FromRowFactoryWithTimestamps, FromValidatedRowModelFactory}

object DatabaseConfigurationFactory extends FromValidatedRowModelFactory[database.DatabaseConfiguration] with Deprecatable
	with FromRowFactoryWithTimestamps[database.DatabaseConfiguration]
{
	// IMPLEMENTED	---------------------
	
	override def table = Tables.databaseConfiguration
	
	override protected def fromValidatedModel(model: Model[Constant]) = DatabaseConfiguration(model("id"),
		model("databaseId"), DatabaseConfigurationData(model("name"), model("creatorId")))
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def creationTimePropertyName = "created"
}


