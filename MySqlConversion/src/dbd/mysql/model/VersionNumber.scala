package dbd.mysql.model

import utopia.flow.generic.ValueConversions._
import utopia.flow.datastructure.immutable.Value
import utopia.flow.util.StringExtensions._

object VersionNumber
{
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
	override def toString = s"v${numbers.mkString(".")}"
}
