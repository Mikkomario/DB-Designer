package dbd.core.model

/**
 * Specifies the name and type of a class
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param name The name of the class
 * @param isMutable Whether the class attributes are mutable (default = false)
 */
case class ClassInfo(name: String, isMutable: Boolean = false)
