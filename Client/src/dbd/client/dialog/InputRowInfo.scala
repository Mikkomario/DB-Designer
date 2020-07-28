package dbd.client.dialog

import utopia.flow.event.Changing
import utopia.reflection.container.swing.layout.multi.Stack.AwtStackable
import utopia.reflection.localization.LocalizedString

/**
 * Contains basic information about a row used in an input dialog
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 * @param fieldName Displayed name of the field
 * @param field Input field or a wrapper
 * @param spansWholeRow Whether the field should be set to span the whole width of the row (default = true)
 * @param rowVisibilityPointer A pointer that determines the visibility state for the row. None if row should be always
 *                             visible.
 */
case class InputRowInfo(fieldName: LocalizedString, field: AwtStackable, spansWholeRow: Boolean = true,
						rowVisibilityPointer: Option[Changing[Boolean]] = None)
