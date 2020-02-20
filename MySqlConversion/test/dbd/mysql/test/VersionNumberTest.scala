package dbd.mysql.test

import dbd.mysql.model.VersionNumber
import utopia.flow.generic.DataType

/**
 * Tests version number parsing
 * @author Mikko Hilpinen
 * @since 31.1.2020, v0.1
 */
object VersionNumberTest extends App
{
	DataType.setup()
	
	val testVersion = VersionNumber(Vector(1, 12, 0, 3))
	assert(VersionNumber.parse("v1.12.0.3") == testVersion)
	assert(testVersion.toString == "v1.12.0.3")
	assert(testVersion.next() == VersionNumber(2))
	assert(testVersion.next(1) == VersionNumber(1, 13))
	assert(testVersion.next(2) == VersionNumber(1, 12, 1))
	
	println("Success!")
}
