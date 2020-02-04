package dbd.core.database.model

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import dbd.core.database.Tables
import dbd.core.model.existing
import dbd.core.model.partial.NewClassInfo
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.nosql.factory.{Deprecatable, RowFactoryWithTimestamps, StorableFactoryWithValidation}
import utopia.vault.model.immutable.StorableWithFactory

object ClassInfo extends StorableFactoryWithValidation[existing.ClassInfo] with Deprecatable
	with RowFactoryWithTimestamps[existing.ClassInfo]
{
	// ATTRIBUTES	----------------------------
	
	override val nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	
	// IMPLEMENTED	----------------------------
	
	override def creationTimePropertyName = "created"
	
	override def table = Tables.classInfo
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.ClassInfo(model("id").getInt,
		model("classId").getInt, model("name").getString, model("isMutable").getBoolean)
	
	
	// COMPUTED	--------------------------------
	
	/**
	 * @return A model that has just been marked as deprecated
	 */
	def deprecatedNow = apply(deprecatedAfter = Some(Instant.now()))
	
	
	// OTHER	--------------------------------
	
	/**
	 * @param classId Id of targeted class
	 * @return A model with only class id set
	 */
	def withClassId(classId: Int) = apply(classId = Some(classId))
	
	/**
	 * Creates a model ready to be inserted to DB
	 * @param classId Id of targeted class
	 * @param newInfo New class information specifics
	 * @return A model ready to be inserted
	 */
	def forInsert(classId: Int, newInfo: NewClassInfo) = apply(None, Some(classId), Some(newInfo.name),
		Some(newInfo.isMutable))
}

/**
 * Used for interacting with class info in DB
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
case class ClassInfo(id: Option[Int] = None, classId: Option[Int] = None, name: Option[String] = None,
					 isMutable: Option[Boolean] = None, deprecatedAfter: Option[Instant] = None,
					 created: Option[Instant] = None)
	extends StorableWithFactory[existing.ClassInfo]
{
	override def factory = ClassInfo
	
	override def valueProperties = Vector("id" -> id, "classId" -> classId, "name" -> name, "isMutable" -> isMutable,
		"deprecatedAfter" -> deprecatedAfter, "created" -> created)
}
