package dbd.mysql.model

/**
 * Represents a version number
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class VersionNumber(numbers: Vector[Int])
{
	override def toString = s"v${numbers.mkString(".")}"
}
