package dbd.client.vc

import utopia.genesis.color.Color
import utopia.genesis.shape.shape2D.{Bounds, Line}
import utopia.genesis.util.Drawer
import utopia.reflection.component.context.TextContextLike
import utopia.reflection.component.drawing.template.CustomDrawer
import utopia.reflection.component.drawing.template.DrawLevel.Normal
import utopia.reflection.component.swing.label.EmptyLabel
import utopia.reflection.shape.StackSize

/**
  * Used for creating components that separate items with a line
  * @author Mikko Hilpinen
  * @since 3.2.2020, v0.1
  */
object SeparatorLine
{
	/**
	  * Creates a new separator line
	  * @param context Component creation context
	  */
	def apply()(implicit context: TextContextLike) =
	{
		val emptyLabel = new EmptyLabel
		val sizedLabel = emptyLabel.withStackSize(StackSize.any.withLowPriority)
		emptyLabel.addCustomDrawer(new RowLineDrawer(context.hintTextColor))
		sizedLabel
	}
	
	private class RowLineDrawer(color: Color) extends CustomDrawer
	{
		override def drawLevel = Normal
		
		override def draw(drawer: Drawer, bounds: Bounds) = drawer.withStroke(3).withEdgeColor(color)
			.draw(Line(bounds.topLeft average bounds.bottomLeft, bounds.topRight average bounds.bottomRight))
	}
}
