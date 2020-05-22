package dbd.api.database.model.mysql

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.existing.mysql
import dbd.core.model.existing.mysql.Release
import dbd.core.model.partial.mysql.NewRelease
import dbd.core.util.VersionNumber
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{FromRowFactoryWithTimestamps, FromValidatedRowModelFactory}

object ReleaseModel extends FromValidatedRowModelFactory[Release] with FromRowFactoryWithTimestamps[Release]
{
	// IMPLEMENTED	-------------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = mysql.Release(model("id").getInt,
		model("databaseId").getInt, VersionNumber.parse(model("versionNumber").getString), model("created").getInstant)
	
	override def table = Tables.release
	
	override def creationTimePropertyName = "created"
	
	
	// OTHER	----------------------------
	
	/**
	  * @param databaseId Id of targeted database
	  * @return A model with only db id set
	  */
	def withDatabaseId(databaseId: Int) = apply(databaseId = Some(databaseId))
	
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
case class ReleaseModel(id: Option[Int] = None, databaseId: Option[Int] = None, versionNumber: Option[VersionNumber] = None,
						released: Option[Instant] = None) extends StorableWithFactory[Release]
{
	override def factory = ReleaseModel
	
	override def valueProperties = Vector("id" -> id, "databaseId" -> databaseId,
		"versionNumber" -> versionNumber.map { _.toString }, "created" -> released)
}
