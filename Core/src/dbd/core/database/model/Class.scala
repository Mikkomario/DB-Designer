package dbd.core.database.model

import dbd.core
import dbd.core.database.Tables
import dbd.core.util.Log
import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.{Result, Storable}
import utopia.vault.model.immutable.factory.{Deprecatable, FromResultFactory}

import scala.util.{Failure, Success}

object Class extends FromResultFactory[core.model.Class] with Deprecatable
{
	// IMPLEMENTED	------------------------------
	
	override def nonDeprecatedCondition = ClassInfo.nonDeprecatedCondition && Attribute.nonDeprecatedCondition
	
	override def table = Tables.classTable
	
	override def joinedTables = ClassInfo.tables ++ Attribute.tables
	
	override def apply(result: Result) =
	{
		// Groups rows based on class
		result.grouped(table, Attribute.table).flatMap { case (_, data) =>
			val (baseRow, attributeRows) = data
			// Class must be parseable
			table.requirementDeclaration.validate(baseRow(table)).toTry match
			{
				case Success(_) =>
					// Class info must be present in row
					ClassInfo(baseRow).map { classInfo =>
						// Parses attribute rows
						val attributes = attributeRows.flatMap { Attribute(_) }
						core.model.Class(classInfo, attributes.toSet)
					}
				case Failure(error) =>
					Log(error, s"Couldn't create class from row $baseRow")
					None
			}
		}.toVector
	}
}

/**
 * Used for interacting with classes in DB
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
case class Class(id: Option[Int] = None) extends Storable
{
	override def table = Class.table
	
	override def valueProperties = Vector("id" -> id)
}
