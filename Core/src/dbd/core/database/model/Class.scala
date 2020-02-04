package dbd.core.database.model

import java.time.Instant

import utopia.flow.util.CollectionExtensions._
import dbd.core.database.Tables
import dbd.core.model.existing
import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.{Result, Storable}
import utopia.vault.nosql.factory.{Deprecatable, FromResultFactory}
import utopia.vault.util.ErrorHandling

import scala.util.{Failure, Success}

object Class extends FromResultFactory[existing.Class] with Deprecatable
{
	// IMPLEMENTED	------------------------------
	
	override def nonDeprecatedCondition = ClassInfo.nonDeprecatedCondition && Attribute.nonDeprecatedCondition &&
		notDeletedCondition
	
	override def table = Tables.classTable
	
	override def joinedTables = ClassInfo.tables ++ Attribute.tables
	
	override def apply(result: Result) =
	{
		// Groups rows based on class
		result.grouped(table, Attribute.table).tryMap { case (id, data) =>
			val (baseRow, attributeRows) = data
			// Class must be parseable
			table.requirementDeclaration.validate(baseRow(table)).toTry.flatMap { valid =>
				// Class info must be present in row
				ClassInfo(baseRow).flatMap { classInfo =>
					// Parses attribute rows
					attributeRows.tryMap { Attribute(_) }.map { attributes =>
						existing.Class(id.getInt, valid("databaseId").getInt, classInfo, attributes.toVector,
							valid("deletedAfter").instant)
					}
				}
			}
		} match
		{
			case Success(classes) => classes.toVector
			case Failure(error) => ErrorHandling.modelParsePrinciple.handle(error); Vector()
		}
	}
	
	
	// COMPUTED	---------------------------------
	
	/**
	 * @return A condition for class to not be deleted yet
	 */
	def notDeletedCondition = deletionTimeColumn.isNull
	
	/**
	  * @return Column that contains class creation time
	  */
	def creationTimeColumn = table("created")
	
	/**
	  * @return Column that contains class deletion time
	  */
	def deletionTimeColumn = table("deletedAfter")
	
	/**
	 * @return A model that has just been marked as deleted
	 */
	def nowDeleted = apply(deletedAfter = Some(Instant.now()))
	
	
	// OTHER	---------------------------------
	
	/**
	 * @param id Class id
	 * @return A model with only class id set
	 */
	def withId(id: Int) = apply(Some(id))
	
	/**
	 * @param databaseId Id of target DB
	 * @return A model with only database id set
	 */
	def withDatabaseId(databaseId: Int) = apply(databaseId = Some(databaseId))
	
	/**
	 * @return A new model ready to be inserted to DB
	 */
	def forInsert(databaseId: Int) = apply(None, Some(databaseId))
}

/**
 * Used for interacting with classes in DB
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
case class Class(id: Option[Int] = None, databaseId: Option[Int] = None, deletedAfter: Option[Instant] = None) extends Storable
{
	// IMPLEMENTED	---------------------------
	
	override def table = Class.table
	
	override def valueProperties = Vector("id" -> id, "databaseId" -> databaseId, "deletedAfter" -> deletedAfter)
	
	
	// COMPUTED	-------------------------------
	
	/**
	 * @return A copy of this model that has just been marked as deleted
	 */
	def nowDeleted = copy(deletedAfter = Some(Instant.now()))
}
