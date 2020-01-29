package dbd.mysql.test

import dbd.core.database.ConnectionPool
import dbd.core.util.ThreadPool
import dbd.mysql.controller.SqlWriter
import dbd.mysql.database.Release
import utopia.flow.generic.DataType

import scala.concurrent.ExecutionContext

/**
 * Tries to export latest recorded DB structure to SQL
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object SqlWriteTest extends App
{
	DataType.setup()
	
	implicit val exc: ExecutionContext = ThreadPool.executionContext
	ConnectionPool { implicit connection =>
		Release.latest match
		{
			case Some(release) =>
				val tables = Release(release.id).tables
				println(s"Writing sql for release ${release.versionNumber} (${tables.size} tables)")
				println()
				println(SqlWriter("test_export", tables))
			case None => println("No released database structure available at this time")
		}
	}
}
