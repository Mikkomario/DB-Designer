package dbd.core.model

/**
 * Specifies the basic structure of a model
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param info Basic info about this class
 * @param attributes Attribute specifications for this class
 */
case class Class(info: ClassInfo, attributes: Set[Attribute])
