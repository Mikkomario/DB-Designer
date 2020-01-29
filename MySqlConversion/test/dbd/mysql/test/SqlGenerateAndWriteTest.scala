package dbd.mysql.test

import dbd.core.database.ConnectionPool
import dbd.core.util.ThreadPool
import dbd.mysql.controller.{GenerateTableStructure, SqlWriter}
import dbd.mysql.model.VersionNumber
import utopia.flow.generic.DataType

import scala.concurrent.ExecutionContext
import scala.io.StdIn

/**
 * Attempts to write current database status to SQL
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object SqlGenerateAndWriteTest extends App
{
	DataType.setup()
	
	private implicit val exc: ExecutionContext = ThreadPool.executionContext
	
	val version = StdIn.readLine("Please insert new version number: ")
	ConnectionPool { implicit connection =>
		val (release, tables) = GenerateTableStructure(1, VersionNumber.parse(version))
		println(s"Generated new release ${release.versionNumber} (id: ${release.id}) with ${tables.size} tables")
		println()
		println(SqlWriter("export_test", tables))
	}
}
