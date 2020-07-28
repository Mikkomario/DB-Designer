package dbd.client.vc

import utopia.reflection.component.context.TextContextLike
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.swing.layout.multi.Stack
import utopia.reflection.localization.LocalizedString
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
	def apply(headerName: LocalizedString)(implicit context: TextContextLike) =
		Stack.buildRowWithContext(isRelated = true) { row =>
			row += TextLabel.contextual(headerName, isHint = true)
			row += SeparatorLine()
		}
}
