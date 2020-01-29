package dbd.core.test

import dbd.core.model.enumeration.NamingConvention.{CamelCase, Capitalized, Underscore}

/**
 * Tests naming conventions
 * @author Mikko Hilpinen
 * @since 27.1.2020, v0.1
 */
object NamingConventionTest extends App
{
	assert(Underscore.accepts("test_name"))
	assert(Underscore.accepts("test"))
	assert(Underscore.accepts("_1_db_name"))
	assert(Underscore.accepts("test_1"))
	
	assert(Underscore.notAccepts("testName"))
	assert(Underscore.notAccepts("1_test"))
	assert(Underscore.notAccepts("Test Name"))
	
	assert(CamelCase.accepts("testName"))
	assert(CamelCase.accepts("test2"))
	assert(CamelCase.accepts("redRGB"))
	assert(CamelCase.accepts("testName2"))
	assert(CamelCase.accepts("_privateVar"))
	
	assert(CamelCase.notAccepts("test_name"))
	assert(CamelCase.notAccepts("3Apples"))
	assert(CamelCase.notAccepts("Monster Factory"))
	
	assert(Capitalized.accepts("3 Apples"))
	assert(Capitalized.accepts("Monster Bunny Man 3"))
	assert(Capitalized.accepts("Red RGB"))
	assert(Capitalized.accepts("Blue"))
	
	assert(Capitalized.notAccepts("bunny"))
	assert(Capitalized.notAccepts("Drop of oil"))
	assert(Capitalized.notAccepts(" Continue"))
	assert(Capitalized.notAccepts("test_name"))
	assert(Capitalized.notAccepts("testName"))
	assert(Capitalized.notAccepts("TestName"))
	
	assert(Underscore.convert("testName") == "test_name")
	assert(Underscore.convert("1_test") == "_1_test")
	assert(Underscore.convert("Test Name") == "test_name")
	assert(Underscore.convert("redRGB") == "red_rgb")
	assert(Underscore.convert("Mapping Attribute") == "mapping_attribute")
	
	assert(CamelCase.convert("test_name") == "testName")
	assert(CamelCase.convert("3Apples") == "_3Apples")
	assert(CamelCase.convert("Monster Factory") == "monsterFactory")
	assert(CamelCase.convert("Last Jedi 3") == "lastJedi3")
	
	assert(Capitalized.convert("bunny") == "Bunny")
	assert(Capitalized.convert("Drop of oil") == "Drop Of Oil")
	assert(Capitalized.convert(" Continue") == "Continue")
	assert(Capitalized.convert("test_name") == "Test Name")
	assert(Capitalized.convert("testName") == "Test Name")
	assert(Capitalized.convert("TestName") == "Test Name")
	assert(Capitalized.convert("redRGB4") == "Red RGB 4")
	
	println("Success!")
}
