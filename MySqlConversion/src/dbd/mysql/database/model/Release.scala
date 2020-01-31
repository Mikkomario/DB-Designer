package dbd.mysql.database.model

import java.time.Instant

import dbd.mysql.database.Tables
import dbd.mysql.model.partial.NewRelease
import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.{VersionNumber, existing}
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactoryWithValidation

object Release extends StorableFactoryWithValidation[existing.Release]
{
	// IMPLEMENTED	-------------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.Release(model("id").getInt,
		model("databaseId").getInt, VersionNumber.parse(model("versionNumber").getString), model("created").getInstant)
	
	override def table = Tables.release
	
	
	// OTHER	----------------------------
	
	/**
	 * @param data Data for insert
	 * @return A model ready to be inserted to DB
	 */
	def forInsert(data: NewRelease) = apply(None, Some(data.databaseId), Some(data.versionNumber), Some(data.released))
}

/**
 * Used for interacting with release DB data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Release(id: Option[Int] = None, databaseId: Option[Int] = None, versionNumber: Option[VersionNumber],
				   released: Option[Instant] = None) extends StorableWithFactory[existing.Release]
{
	override def factory = Release
	
	override def valueProperties = Vector("id" -> id, "databaseId" -> databaseId,
		"versionNumber" -> versionNumber.map { _.toString }, "created" -> released)
}
