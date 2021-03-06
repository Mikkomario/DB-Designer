package dbd.core.model.enumeration

import utopia.flow.util.CollectionExtensions._
import dbd.core.model.error.NoSuchTypeException
import utopia.flow.generic
import utopia.flow.generic.{DataType, StringType}

/**
 * Determines the data type of an attribute
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 */
sealed trait AttributeType
{
	/**
	 * The data type matching this attribute type
	 */
	val dataType: DataType
	/**
	 * A unique id for this type
	 */
	val id: Int
}

object AttributeType
{
	/**
	 * Represents a short piece of text
	 */
	object ShortStringType extends AttributeType
	{
		override val dataType = StringType
		override val id = 1
		override def toString = "Short Text"
	}
	
	/**
	 * Represents an integer number
	 */
	object IntType extends AttributeType
	{
		override val dataType = generic.IntType
		
		override val id = 2
		
		override def toString = "Integer Number"
	}
	
	/**
	 * Represents a double number
	 */
	object DoubleType extends AttributeType
	{
		override val dataType = generic.DoubleType
		
		override val id = 3
		
		override def toString = "Decimal Number"
	}
	
	/**
	 * Represents a boolean
	 */
	object BooleanType extends AttributeType
	{
		override val dataType = generic.BooleanType
		
		override val id = 4
		
		override def toString = "True/False"
	}
	
	object InstantType extends AttributeType
	{
		override val dataType = generic.InstantType
		override val id = 5
		override def toString = "Date+Time"
	}
	
	
	// OTHER	-----------------------
	
	/**
	 * All introduced attribute types
	 */
	val values = Vector(ShortStringType, IntType, DoubleType, BooleanType, InstantType)
	
	/**
	 * @param id Target id
	 * @return An attribute type for the id. None if not found
	 */
	def forId(id: Int) = values.find { _.id == id }
	
	/**
	 * @param id Target id
	 * @return An attribute type for the id. Failure if id didn't match any attribute type
	 */
	def withId(id: Int) = forId(id).toTry(new NoSuchTypeException(s"No attribute type for id $id"))
}