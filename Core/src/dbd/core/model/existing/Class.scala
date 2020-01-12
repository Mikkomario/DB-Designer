package dbd.core.model.existing

import dbd.core.model.template.ClassLike

/**
 * Specifies the basic structure of a model
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param id Unique id of this class
 * @param info Basic info about this class
 * @param attributes Attribute specifications for this class
 */
case class Class(id: Int, info: ClassInfo, attributes: Vector[Attribute]) extends ClassLike[ClassInfo, Attribute]
