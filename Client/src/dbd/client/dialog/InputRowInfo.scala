package dbd.client.dialog

import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.localization.LocalizedString

/**
 * Contains basic information about a row used in an input dialog
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 * @param fieldName Displayed name of the field
 * @param field Input field or a wrapper
 * @param spansWholeRow Whether the field should be set to span the whole width of the row (default = true)
 */
case class InputRowInfo(fieldName: LocalizedString, field: AwtStackable, spansWholeRow: Boolean = true)
