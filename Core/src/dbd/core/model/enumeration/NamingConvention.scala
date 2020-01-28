package dbd.core.model.enumeration

import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._

/**
 * Represents a way of naming entities
 * @author Mikko Hilpinen
 * @since 27.1.2020, v0.1
 */
sealed trait NamingConvention
{
	// ABSTRACT	-----------------
	
	/**
	 * @param name A name
	 * @return Whether the specified name conforms to this naming convention
	 */
	def accepts(name: String): Boolean
	/**
	 * @param name A name
	 * @return A version of the name that is converted to this naming convention
	 */
	def convert(name: String): String
	
	
	// OTHER	-----------------
	
	/**
	 * @param name A name
	 * @return Whether the specified name doesn't follow this conventions rules
	 */
	def notAccepts(name: String) = !accepts(name)
}

object NamingConvention
{
	/**
	 * A naming convention often used in database context
	 */
	case object Underscore extends NamingConvention
	{
		override def accepts(name: String) = name.forall { c => (c == '_' || c.isDigit || c.isLower) && c != ' ' } &&
			name.headOption.forall { !_.isDigit }
		
		override def convert(name: String) =
		{
			if (accepts(name))
				name
			else
			{
				// Underscore name cannot start with a digit
				val withoutStartDigit = if (name.head.isDigit) "_" + name else name
				// Converts all whitespaces to _
				val withoutWhiteSpace = withoutStartDigit.replaceAll(" ", "_")
				// Prepends an underscore before each uppercase letter that comes after a non _ character
				val builder = new StringBuilder
				// Starts with a lower case character
				builder += withoutWhiteSpace.head.toLower
				var remaining = withoutWhiteSpace.drop(1)
				while (remaining.nonEmpty)
				{
					val nextPart = remaining.takeWhile { c => c.isLower || c == '_' }
					if (nextPart.isEmpty)
					{
						// If multiple uppercase characters are added simultaneously, they are simply converted to lowercase
						builder += remaining.head.toLower
						remaining = remaining.drop(1)
					}
					else if (nextPart.length == remaining.length)
					{
						// If rest of the string is lower case, simply adds it
						builder ++= nextPart
						remaining = ""
					}
					else if (nextPart.endsWith("_"))
					{
						// If uppercase letter is prepended with _, simply converts it to lower case
						builder ++= nextPart
						builder += remaining(nextPart.length).toLower
						remaining = remaining.drop(nextPart.length + 1)
					}
					else
					{
						// Otherwise adds a _ before the uppercase letter and converts it to lower case
						builder ++= nextPart
						builder += '_'
						builder += remaining(nextPart.length).toLower
						remaining = remaining.drop(nextPart.length + 1)
					}
				}
				builder.result()
			}
		}
	}
	
	/**
	 * A naming convention often used in code context
	 */
	case object CamelCase extends NamingConvention
	{
		// Camel case names don't accept _ except at the beginning of the name
		override def accepts(name: String) = !name.contains(' ') && name.headOption.forall { !_.isDigit } &&
			!name.drop(1).contains('_')
		
		override def convert(name: String) =
		{
			if (accepts(name))
				name
			else
			{
				// Name cannot start with a digit
				val withoutStartDigit = if (name.head.isDigit) "_" + name else name
				val builder = new StringBuilder
				var remaining = withoutStartDigit
				
				// Allows one underscore at the beginning of the name
				if (remaining.startsWith("_"))
				{
					builder += remaining.head
					remaining = remaining.drop(1)
				}
				// Name always starts with a lower case character
				if (remaining.headOption.exists { _.isUpper })
				{
					val nextPart = remaining.takeWhile { _.isUpper }.toLowerCase
					builder ++= nextPart
					remaining = remaining.drop(nextPart.length)
				}
				
				while (remaining.nonEmpty)
				{
					// Finds the next whitespace or underscore, removes it and replaces the next character with an
					// uppercase one
					val nextPart = remaining.takeWhile { c => c != '_' && c != ' ' }
					if (nextPart.length >= remaining.length - 1)
					{
						builder ++= nextPart
						remaining = ""
					}
					else
					{
						builder ++= nextPart
						val numberOfSkipped = remaining.drop(nextPart.length + 1).takeWhile { c => c == '_' || c == ' ' }.length + 1
						val nextValidIndex = nextPart.length + numberOfSkipped
						if (remaining.length > nextValidIndex)
						{
							builder += remaining(nextValidIndex).toUpper
							remaining = remaining.drop(nextValidIndex + 1)
						}
						else
							remaining = ""
					}
				}
				builder.result()
			}
		}
	}
	
	/**
	 * A naming convention mostly used when displaying names in human-readable format
	 */
	case object Capitalized extends NamingConvention
	{
		// Capitalized style doesn't accept underscores or lower case letters followed by upper case letters
		override def accepts(name: String) = !name.startsWith(" ") && !name.contains('_') &&
			name.words.forall { w => w.head.isDigit || w.head.isUpper  } && name.toVector.paired.forall {
			case (first, second) => first == ' ' || (first.isDigit && (second.isDigit || second == ' ')) ||
				!(first.isLower && second.isUpper) }
		
		override def convert(name: String) =
		{
			if (accepts(name))
				name
			else
			{
				// Replaces underscore with whitespace and then splits camel casing inside the words
				// Also removes an leading whitespace
				val words = name.replaceAll("_", " ").dropWhile { _ == ' ' }.words
				val nonCamelWords = words.flatMap(convertWord)
				
				// Combines the words together with whitespace
				nonCamelWords.mkString(" ")
			}
		}
		
		private def convertWord(word: String): Vector[String] =
		{
			if (word.length < 2)
				Vector(word)
			else
			{
				val start =
				{
					// Allows multiple consequent uppercase letters or digits in the beginning, but will split the word afterward
					if (word.head.isDigit)
						word.takeWhile { !_.isLetter }
					else if (word.take(2).forall { _.isUpper })
						word.takeWhile { c => c.isUpper && !c.isDigit }
					else
						word.head.toUpper + word.tail.takeWhile { c => c.isLower && !c.isDigit }
				}
				
				if (start.length == word.length)
					Vector(start)
				else
				{
					// Handles the remaining portion recursively
					start +: convertWord(word.drop(start.length))
				}
			}
		}
	}
}