package dbd.client.vc

import utopia.genesis.color.Color
import utopia.genesis.shape.shape2D.{Bounds, Line}
import utopia.genesis.util.Drawer
import utopia.reflection.component.drawing.CustomDrawer
import utopia.reflection.component.drawing.DrawLevel.Normal
import utopia.reflection.component.swing.label.{EmptyLabel, TextLabel}
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.StackSize
import utopia.reflection.util.ComponentContext

/**
 * These components are used as small headers for groups of items
 * @author Mikko Hilpinen
 * @since 22.1.2020, v0.1
 */
object GroupHeader
{
	/**
	 * Creates a new group header
	 * @param headerName The name displayed on the header (localized)
	 * @param context Component creation context
	 * @return A new header component
	 */
	def apply(headerName: LocalizedString)(implicit context: ComponentContext) =
		Stack.buildRowWithContext(isRelated = true) { row =>
			row += TextLabel.contextual(headerName, isHint = true)
			val emptyLabel = new EmptyLabel
			row += emptyLabel.withStackSize(StackSize.any.withLowPriority)
			emptyLabel.addCustomDrawer(new RowLineDrawer(context.hintTextColor))
		}
	
	private class RowLineDrawer(color: Color) extends CustomDrawer
	{
		override def drawLevel = Normal
		
		override def draw(drawer: Drawer, bounds: Bounds) = drawer.withStroke(3).withEdgeColor(color)
			.draw(Line(bounds.topLeft average bounds.bottomLeft, bounds.topRight average bounds.bottomRight))
	}
}
