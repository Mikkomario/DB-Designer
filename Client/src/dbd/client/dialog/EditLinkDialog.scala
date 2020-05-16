package dbd.client.dialog

import dbd.client.controller.Icons
import dbd.client.view.Fields
import utopia.reflection.localization.LocalString._
import dbd.core.model.enumeration.LinkType
import dbd.core.model.existing
import dbd.core.model.existing.database.{Attribute, LinkConfiguration}
import dbd.core.model.partial.database
import dbd.core.model.partial.database.NewLinkConfiguration
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.component.Focusable
import utopia.reflection.component.context.ButtonContext
import utopia.reflection.component.swing.TextField
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.LengthExtensions._

import scala.collection.immutable.HashMap

/**
 * Used for adding & editing links
 * @author Mikko Hilpinen
 * @since 21.1.2020, v0.1
 */
class EditLinkDialog(linkToEdit: Option[LinkConfiguration], linkingClass: existing.database.Class, linkableClasses: Vector[existing.database.Class])
	extends InputDialog[Option[NewLinkConfiguration]]
{
	// ATTRIBUTES	-----------------------
	
	import dbd.client.view.DefaultContext._
	
	private implicit val language: String = "en"
	private implicit val context: ButtonContext = baseContext.inContextWithBackground(dialogBackground)
		.forTextComponents().forGrayFields
	
	private val mapKeyVisibilityPointer = new PointerWithEvents[Boolean](false)
	
	private val classSelection = Fields.searchFrom[existing.database.Class]("No class with name: '%s'",
		"Select linked class", DisplayFunction.noLocalization[existing.database.Class] { _.name })
	private val linkOriginSelection = Fields.dropDown("No choices available", "Select link origin",
		DisplayFunction.functionToDisplayFunction[Boolean] { isThisClass =>
			if (isThisClass) linkingClass.name.noLanguageLocalizationSkipped else classSelection.value.map {
				_.name.noLanguageLocalizationSkipped }.getOrElse("The other class") }, Vector(true, false))
	private val linkTypeSelection = Fields.searchFromWithIcons[LinkType]("No link type named '%s'", "Select link type",
		DisplayFunction[LinkType] { _.nameWithClassSlots } { local =>
			// Link type display depends from which class was selected as the link origin
			val myClassName = linkingClass.name
			val otherClassName = classSelection.value.map { _.name }.getOrElse("?")
			val (originName, targetName) =
			{
				if (linkOriginSelection.value.getOrElse(true))
					myClassName -> otherClassName
				else
					otherClassName -> myClassName
			}
			local.localized.interpolated(HashMap("origin" -> originName, "target" -> targetName)) },
		LinkType.values) { lType => Icons.forLinkType(lType.category) }
	
	private val mapKeySelection = Fields.searchFromWithIcons[Attribute]("No attribute matching '%s'",
		"Select map key", DisplayFunction.noLocalization[Attribute] { a => a.name }) {
		a => Icons.forAttributeType(a.dataType) }
	private val localNickField = TextField.contextual(standardInputWidth.any, prompt = Some("Link name, optional"))
	private val otherNickField = TextField.contextual(standardInputWidth.any, prompt = Some("Link name, optional"))
	
	
	// INITIAL CODE	-----------------------
	
	classSelection.content = linkableClasses
	
	linkOriginSelection.selectOne(true)
	
	// When link origin changes, so does link type selection and mapped key options
	linkOriginSelection.addValueListener { _ =>
		updateMapKeySelection()
		updateLinkTypeSelection()
	}
	
	// Whenever linked class is changed, link type selection look is updated as well
	classSelection.addValueListener { _ =>
		linkOriginSelection.currentDisplays.foreach { _.refreshText() }
		updateMapKeySelection()
		updateLinkTypeSelection()
	}
	
	// Whenever a map-using link type is selected, displays a selection for map key attribute
	linkTypeSelection.addValueListener { _ => updateMapKeySelection() }
	
	// Sets initial values
	linkToEdit.foreach { link =>
		val thisClassIsOrigin = link.originClassId == linkingClass.id
		val otherClassId = if (thisClassIsOrigin) link.targetClassId else link.originClassId
		val otherClass = linkableClasses.find { _.id == otherClassId }
		classSelection.value = otherClass
		linkOriginSelection.selectOne(thisClassIsOrigin)
		linkTypeSelection.selectOne(link.linkType)
		mapKeySelection.value = link.mappingKeyAttributeId.flatMap { attId =>
			otherClass.flatMap { _.attributes.find { _.id == attId } } }
		val (localNick, otherNick) = if (thisClassIsOrigin) link.nameInOrigin -> link.nameInTarget else
			link.nameInTarget -> link.nameInOrigin
		localNick.foreach { localNickField.text = _ }
		otherNick.foreach { otherNickField.text = _ }
	}
	
	
	// COMPUTED	---------------------------
	
	private def originatesCurrentlyFromThisClass = linkOriginSelection.value.getOrElse(true)
	
	private def currentTargetClass =
	{
		if (originatesCurrentlyFromThisClass)
			Some(linkingClass)
		else
			classSelection.value
	}
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def fields = Vector(
		InputRowInfo("Linked Class", classSelection),
		InputRowInfo("Link starts from", linkOriginSelection),
		InputRowInfo("Link Type", linkTypeSelection),
		InputRowInfo("Map Key", mapKeySelection, rowVisibilityPointer = Some(mapKeyVisibilityPointer)),
		InputRowInfo("Name in this class", localNickField),
		InputRowInfo("Name in linked class", otherNickField)
	)
	
	override protected def produceResult: Either[(AwtStackable with Focusable, LocalizedString), Option[NewLinkConfiguration]] =
	{
		classSelection.value match
		{
			case Some(otherClass) =>
				linkTypeSelection.value match
				{
					case Some(linkType) =>
						// Map key is required when link type uses mapping
						val selectedMapKey = mapKeySelection.value
						if (linkType.usesMapping && selectedMapKey.isEmpty)
							Left(mapKeySelection -> "Please specify which attribute should be used as the mapping key")
						else
						{
							val actualMappingKey = if (linkType.usesMapping) selectedMapKey.map { _.id } else None
							val originatesFromThisClass = originatesCurrentlyFromThisClass
							val (originId, targetId) =
							{
								if (originatesFromThisClass)
									linkingClass.id -> otherClass.id
								else
									otherClass.id -> linkingClass.id
							}
							val (originNick, targetNick) =
							{
								if (originatesFromThisClass)
									localNickField.value -> otherNickField.value
								else
									otherNickField.value -> localNickField.value
							}
							
							val newLink = database.NewLinkConfiguration(linkType, originId, targetId, originNick, targetNick,
								mappingKeyAttributeId = actualMappingKey)
							if (linkToEdit.exists { _ ~== newLink })
								Right(None)
							else
								Right(Some(newLink))
						}
					case None => Left(linkTypeSelection -> "Please specify the link type")
				}
			case None => Left(classSelection -> "Please select the linked class")
		}
	}
	
	override protected def defaultResult = None
	
	override protected def title = if (linkToEdit.isDefined) "Edit Link" else "Add Link"
	
	
	// OTHER	---------------------------
	
	private def updateLinkTypeSelection() =
	{
		// Mapping link style is allowed when target class contains attributes
		val newLinkTypes = if (currentTargetClass.exists { _.attributes.nonEmpty })
			LinkType.values else LinkType.values.filterNot { _.usesMapping }
		if (linkTypeSelection.content == newLinkTypes)
			linkTypeSelection.currentDisplays.foreach { _.refreshText() }
		else
			linkTypeSelection.content = newLinkTypes
	}
	
	private def updateMapKeySelection() =
	{
		if (linkTypeSelection.value.exists { _.usesMapping })
		{
			currentTargetClass match
			{
				case Some(targetClass) =>
					mapKeySelection.content = targetClass.attributes
					mapKeyVisibilityPointer.value = true
				case None => mapKeyVisibilityPointer.value = false
			}
		}
		else
			mapKeyVisibilityPointer.value = false
	}
}
