package dbd.core.database.model

import java.time.Instant

import dbd.core.database.Tables
import dbd.core.model.existing
import dbd.core.util.Log
import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.{Result, Storable}
import utopia.vault.model.immutable.factory.{Deprecatable, FromResultFactory}

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
		result.grouped(table, Attribute.table).flatMap { case (id, data) =>
			val (baseRow, attributeRows) = data
			// Class must be parseable
			table.requirementDeclaration.validate(baseRow(table)).toTry match
			{
				case Success(valid) =>
					// Class info must be present in row
					ClassInfo(baseRow).map { classInfo =>
						// Parses attribute rows
						val attributes = attributeRows.flatMap { Attribute(_) }.toVector
						existing.Class(id.getInt, classInfo, attributes, valid("deletedAfter").instant)
					}
				case Failure(error) =>
					Log(error, s"Couldn't create class from row $baseRow")
					None
			}
		}.toVector
	}
	
	
	// COMPUTED	---------------------------------
	
	/**
	 * @return A condition for class to not be deleted yet
	 */
	def notDeletedCondition = table("deletedAfter").isNull
	
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
	 * @return A new model ready to be inserted to DB
	 */
	def forInsert() = apply()
}

/**
 * Used for interacting with classes in DB
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
case class Class(id: Option[Int] = None, deletedAfter: Option[Instant] = None) extends Storable
{
	// IMPLEMENTED	---------------------------
	
	override def table = Class.table
	
	override def valueProperties = Vector("id" -> id, "deletedAfter" -> deletedAfter)
	
	
	// COMPUTED	-------------------------------
	
	/**
	 * @return A copy of this model that has just been marked as deleted
	 */
	def nowDeleted = copy(deletedAfter = Some(Instant.now()))
}
