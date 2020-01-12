package dbd.core.database.model

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import dbd.core.database.Tables
import dbd.core.model.existing
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.factory.{Deprecatable, StorableFactoryWithValidation}
import utopia.vault.model.immutable.StorableWithFactory

object ClassInfo extends StorableFactoryWithValidation[existing.ClassInfo] with Deprecatable
{
	// ATTRIBUTES	----------------------------
	
	override val nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	
	// IMPLEMENTED	----------------------------
	
	override def table = Tables.classInfo
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.ClassInfo(model("id").getInt,
		model("classId").getInt, model("name").getString, model("isMutable").getBoolean)
}

/**
 * Used for interacting with class info in DB
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
case class ClassInfo(id: Option[Int] = None, classId: Option[Int] = None, name: Option[String] = None,
					 isMutable: Option[Boolean] = None, deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[existing.ClassInfo]
{
	override def factory = ClassInfo
	
	override def valueProperties = Vector("id" -> id, "classId" -> classId, "name" -> name, "isMutable" -> isMutable,
		"deprecatedAfter" -> deprecatedAfter)
}
