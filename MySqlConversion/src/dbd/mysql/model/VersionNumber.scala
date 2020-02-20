package dbd.mysql.model

import utopia.flow.util.CollectionExtensions._
import utopia.flow.generic.ValueConversions._
import utopia.flow.datastructure.immutable.Value
import utopia.flow.util.StringExtensions._

object VersionNumber
{
	/**
	  * @param firstNumber The primary version number
	  * @param moreNumbers More version numbers
	  * @return A version number
	  */
	def apply(firstNumber: Int, moreNumbers: Int*) = new VersionNumber((firstNumber +: moreNumbers).dropRightWhile { _ == 0 }.toVector)
	
	/**
	 * Parses a version number from a string
	 * @param versionString A version number string
	 * @return parsed number
	 */
	def parse(versionString: String) = apply(versionString.split("\\.").map {
		_.digits: Value }.flatMap { _.int }.toVector)
}

/**
 * Represents a version number
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class VersionNumber(numbers: Vector[Int])
{
	// IMPLEMENTED	----------------------
	
	override def toString = s"v${numbers.mkString(".")}"
	
	
	// OTHER	--------------------------
	
	/**
	  * @param updateLevel Level of update where 0 means the first number and 1 means the second and so on
	  * @return An increased copy of this version number
	  */
	def next(updateLevel: Int = 0) =
	{
		if (numbers.size < updateLevel)
			VersionNumber(numbers.padTo(updateLevel, 0) :+ 1)
		else
			VersionNumber(numbers.take(updateLevel) :+ (numbers(updateLevel) + 1))
	}
}
