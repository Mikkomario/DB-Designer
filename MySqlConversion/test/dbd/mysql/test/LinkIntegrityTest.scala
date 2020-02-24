package dbd.mysql.test

import utopia.flow.util.CollectionExtensions._
import dbd.core.database.ConnectionPool
import dbd.core.util.ThreadPool
import dbd.mysql.database.{Release, Releases}
import utopia.flow.generic.DataType

import scala.concurrent.ExecutionContext

/**
  * Tests whether column links in database can be relied on
  * @author Mikko Hilpinen
  * @since 24.2.2020, v0.1
  */
object LinkIntegrityTest extends App
{
	DataType.setup()
	
	implicit val exc: ExecutionContext = ThreadPool.executionContext
	ConnectionPool { implicit connection =>
		// Checks all releases
		Releases.ids.all.foreach { releaseId =>
			println(s"Checking release $releaseId")
			// Checks all tables in all releases
			Release(releaseId).tables.foreach { table =>
				val classId = table.classId
				val links = table.columns.flatMap { _.linkedData.leftOption }
				if (links.exists { _.linkConfiguration.oppositeClassId(classId).isEmpty })
				{
					println(s"Problem in table: ${table.name} (${table.id}) in release $releaseId (class id = $classId): ")
					links.foreach { l => println(s"- ${l.linkConfiguration.originClassId} -> ${l.linkConfiguration.targetClassId}") }
				}
			}
		}
	}
	
	println("Done!")
}
