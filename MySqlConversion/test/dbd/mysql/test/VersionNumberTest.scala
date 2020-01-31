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
	
	assert(VersionNumber.parse("v1.12.0.3") == VersionNumber(Vector(1, 12, 0, 3)))
	
	println("Success!")
}
