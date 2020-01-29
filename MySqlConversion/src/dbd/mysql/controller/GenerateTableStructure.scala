package dbd.mysql.controller

import java.time.Instant

import dbd.mysql.database
import dbd.core.model.enumeration.NamingConvention._
import utopia.flow.util.CollectionExtensions._
import dbd.core.database.Database
import dbd.core.model.enumeration.AttributeType.IntType
import dbd.core.model.enumeration.LinkTypeCategory.ManyToMany
import dbd.core.model.existing.{Attribute, Class, Link}
import dbd.mysql.model.VersionNumber
import dbd.mysql.model.existing.{Release, Table}
import dbd.mysql.model.partial.{NewColumn, NewColumnAttributeLink, NewColumnLinkLink, NewForeignKey, NewIndex, NewRelease, NewTable}
import utopia.vault.database.Connection

/**
 * Generates table structure based on the latest class structure
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object GenerateTableStructure
{
	// TODO: Add support for many-to-many links
	
	/**
	 * Inserts new table structure to database and returns the generated structure
	 * @param databaseId Id of targeted database
	 * @param newVersionNumber Version number of this release
	 * @param connection DB Connection
	 * @return Generated release + Generated inserted tables
	 */
	def apply(databaseId: Int, newVersionNumber: VersionNumber)(implicit connection: Connection) =
	{
		// Reads class and link data from the DB first
		val classes = Database(databaseId).classes.get
		// TODO: Handle many-to-many -links separately
		val links = Database(databaseId).links.get.filterNot { _.linkType.category == ManyToMany }
		val linksPerClassId = links.groupBy { _.originClassId }
		
		// Creates class names and unique index name prefixes
		val names = classNames(classes)
		val prefixes = uniquePrefixes(names, 0) // 1 prefix per class id
		
		// Converts class data into tables & columns
		val newData = classes.map { c => classToTable(c, names(c.id), prefixes(c.id),
			linksPerClassId.get(c.id).exists { _.exists { link => link.linkType.usesDeprecation } }) }
		
		// Creates a new release
		val newRelease = NewRelease(databaseId, newVersionNumber, newData)
		
		// Inserts the first batch of data to DB
		val newReleaseId = database.model.Release.forInsert(newRelease).insert().getInt
		val storedTables = newData.map { table => database.model.Table.insert(newReleaseId, table) }
		
		// Generates link columns and foreign key data and inserts those to DB as well
		val tablesForClassIds = storedTables.map { table => table.classId -> table }.toMap
		
		val insertedColumnsPerTableId = linksPerClassId.map { case (classId, links) =>
			val table = tablesForClassIds(classId)
			val insertedColumns = insertColumnsAndForeignKeys(table, links, prefixes(classId), tablesForClassIds)
			table.id -> insertedColumns
		}
		
		// Combines inserted data and returns it
		val fullTables = storedTables.map { table =>
			insertedColumnsPerTableId.get(table.id) match
			{
				case Some(additionalColumns) => table.copy(columns = additionalColumns ++ table.columns)
				case None => table
			}
		}
		
		Release(newReleaseId, databaseId, newVersionNumber, Instant.now()) -> fullTables
	}
	
	// Converts class names to underscore style and makes sure each one is unique
	private def classNames(classes: Vector[Class]) =
		makeUnique(classes.toMultiMap(_.name.toUnderscore, _.id))
	
	private def makeUnique(originalData: Map[String, Seq[Int]]) =
	{
		val (uniqueData, nonUniqueData) = originalData.divideBy { _._2.size > 1 }
		
		// Adds numbers to non-unique table names
		val fixedDuplicates = nonUniqueData.flatMap { case (name, ids) => ids.mapWithIndex { (id, index) =>
			id -> s"${name}_$index" } }
		
		// Returns now unique names
		// TODO: Add handling on the unlikely cases where fixed duplicates conflict with existing names
		uniqueData.map { case (name, ids) => ids.head -> name } ++ fixedDuplicates
	}
	
	private def uniquePrefixes(classNames: Map[Int, String], commonLetters: Int): Map[Int, String] =
	{
		// Creates shorter prefix names for the classes
		val shortNames = classNames.map { case (c, name) => c -> nameParts(name, commonLetters + 1) }
		
		// Some shorter names may be duplicates, in which case needs to use more letters
		val classesPerName = shortNames.toVector.map { case (id, name) => name -> id }.toMultiMap()
		val uniqueIds = classesPerName.filter { _._2.size == 1 }.map { _._2.head }.toSet
		
		// Expects class names to be unique
		val handledDuplicates = uniquePrefixes(classNames.filterKeys { !uniqueIds.contains(_) }, commonLetters + 1)
		
		shortNames.filterKeys(uniqueIds.contains) ++ handledDuplicates
	}
	
	// Expects name to be in underscore style
	private def nameParts(name: String, maxLettersPerPart: Int) =
	{
		val parts = name.split("_")
		parts.map { _.take(maxLettersPerPart) }.reduce { _ + _ }
	}
	
	private def classToTable(classToConvert: Class, tableName: String, namePrefix: String, useDeprecation: Boolean) =
	{
		val attNames = attributeNames(classToConvert.attributes)
		NewTable(classToConvert.id, tableName, useDeprecation, classToConvert.isMutable,
			classToConvert.attributes.map { a => attributeToColumn(a, attNames(a.id), namePrefix) })
	}
	
	private def attributeNames(attributes: Vector[Attribute]) =
		makeUnique(attributes.toMultiMap(_.name.toUnderscore, _.id))
	
	private def attributeToColumn(attribute: Attribute, attributeName: String, namePrefix: String) =
	{
		val index = if (attribute.isSearchKey) Some(NewIndex(s"${namePrefix}_${attributeName}_idx")) else None
		NewColumn(Right(NewColumnAttributeLink(attribute.id, index)), attributeName, attribute.dataType, attribute.isOptional)
	}
	
	private def insertColumnsAndForeignKeys(table: Table, links: Vector[Link], prefix: String,
											tablesForClassIds: Map[Int, Table])
										   (implicit connection: Connection) =
	{
		// Link id -> target table
		val linkTargets = links.map { link => link.id -> tablesForClassIds(link.targetClassId) }.toMap
		// If there are multiple links to a single table, the link names are made unique
		// Link id -> link name
		val linkNames = makeUnique(links.toMultiMap(l => linkTargets(l.id).name, _.id))
		
		// Generates columns and inserts the to table
		links.map { link => linkToColumn(link, linkNames(link.id), prefix, linkTargets(link.targetClassId)) }.map {
			database.model.Column.insert(table.id, _) }
	}
	
	private def linkToColumn(link: Link, linkName: String, namePrefix: String, targetTable: Table) =
	{
		// Creates foreign key first
		val fk = NewForeignKey(targetTable.id, s"${namePrefix}_$linkName")
		val linkConnection = NewColumnLinkLink(link.id, fk)
		NewColumn(Left(linkConnection), linkName + "_id", IntType, link.linkType.isOptional)
	}
}
