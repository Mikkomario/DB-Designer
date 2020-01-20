package dbd.client.vc

import dbd.client.controller.ClassDisplayManager
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.DisplayedLink
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.shape.Margins
import utopia.reflection.util.ComponentContextBuilder

/**
 * Displays a number of links
 * @author Mikko Hilpinen
 * @since 20.1.2020, v0.1
 */
class LinksVC(initialClassId: Int, initialLinks: Vector[DisplayedLink], classManager: ClassDisplayManager)
			 (implicit margins: Margins, baseCB: ComponentContextBuilder, colorScheme: ColorScheme)
	extends StackableAwtComponentWrapperWrapper with Refreshable[(Int, Vector[DisplayedLink])]
{
	// ATTRIBUTES	------------------------
	
	private var _content = initialClassId -> initialLinks
	
	private val stack = Stack.row[LinkRowVC](margins.small.downscaling)
	private val manager = new ContainerContentManager[(Int, DisplayedLink), Stack[LinkRowVC], LinkRowVC](stack)({
		case (id, link) => new LinkRowVC(id, link, classManager) })
	
	
	// INITIAL CODE	------------------------
	
	manager.content = initialLinks.map { initialClassId -> _ }
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = stack
	
	override def content_=(newContent: (Int, Vector[DisplayedLink])) =
	{
		_content = newContent
		manager.content = newContent._2.map { newContent._1 -> _ }
	}
	
	override def content = _content
}
