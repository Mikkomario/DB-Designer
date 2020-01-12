package dbd.client.vc

import dbd.core.database
import dbd.client.controller.ConnectionPool
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.Fonts
import dbd.core.model.existing.Class
import dbd.core.model.partial.NewAttribute
import dbd.core.util.Log
import utopia.genesis.color.Color
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ColorScheme, ComponentContextBuilder}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Displays interactive UI for a class
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class ClassVC(initialClass: Class)
			 (implicit baseCB: ComponentContextBuilder, fonts: Fonts, margins: Margins, colorScheme: ColorScheme,
			  defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Class]
{
	// ATTRIBUTES	------------------------
	
	// private implicit val baseContext: ComponentContext = baseCB.result
	
	private val header = ItemLabel.contextual(initialClass, DisplayFunction.noLocalization[Class] { _.info.name })(
		baseCB.copy(textColor = Color.white, font = fonts.header, background = Some(colorScheme.primary)).result)
	private val attributeSection = new AttributesVC(newAttributeAdded)
	
	private val view = Stack.columnWithItems(Vector(header, attributeSection), margin = 0.fixed)
	
	
	// INITIAL CODE	------------------------
	
	// TODO: Add ordering
	attributeSection.content = initialClass.attributes
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Class) =
	{
		header.content = newContent
		attributeSection.content = newContent.attributes // TODO: Add ordering
	}
	
	override def content = header.content
	
	
	// OTHER	---------------------------
	
	private def newAttributeAdded(attribute: NewAttribute): Unit =
	{
		// Inserts attribute data to DB, then updates this view
		ConnectionPool.tryWith { implicit connection =>
			database.Class(content.id).attributes.insert(attribute)
		} match
		{
			case Success(savedAttribute) => content += savedAttribute
			case Failure(error) =>
				Log(error, s"Failed to insert a new attribute ($attribute) for class $content")
		}
	}
}
