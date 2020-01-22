package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.core.model.enumeration.LinkType
import dbd.core.model.existing.{Attribute, Class, LinkConfiguration}
import dbd.core.model.partial.NewLinkConfiguration
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.{DropDown, TextField}
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Used for adding & editing links
 * @author Mikko Hilpinen
 * @since 21.1.2020, v0.1
 */
class EditLinkDialog(linkToEdit: Option[LinkConfiguration], linkingClass: Class, linkableClasses: Vector[Class])
					(implicit baseCB: ComponentContextBuilder, colorScheme: ColorScheme, margins: Margins,
					 exc: ExecutionContext, localizer: Localizer)
	extends InputDialog[Option[NewLinkConfiguration]]
{
	// ATTRIBUTES	-----------------------
	
	private implicit val language: String = "en"
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val mapKeyVisibilityPointer = new PointerWithEvents[Boolean](false)
	
	private val classSelection = DropDown.contextual("Select linked class",
		DisplayFunction.noLocalization[Class] { _.name }, linkableClasses)
	private val linkOriginSelection = DropDown.contextual("Select link origin",
		DisplayFunction.functionToDisplayFunction[Boolean] { isThisClass =>
			if (isThisClass) linkingClass.name.noLanguageLocalizationSkipped else classSelection.value.map {
				_.name.noLanguageLocalizationSkipped }.getOrElse("The other class") }, Vector(true, false))
	private val linkTypeSelection = DropDown.contextual("Select link type",
		DisplayFunction[LinkType] { _.nameWithClassSlots } { local =>
			// Link type display depends from which class was selected as the link origin
			val myClassName = linkingClass.name
			val otherClassName = classSelection.value.map { _.name }.getOrElse("?")
			val (first, second) =
			{
				if (linkOriginSelection.value.getOrElse(true))
					myClassName -> otherClassName
				else
					otherClassName -> myClassName
			}
			local.localized.interpolate(first, second) }, LinkType.values)
	private val mapKeySelection = DropDown.contextual[Attribute]("Select map key",
		DisplayFunction.noLocalization[Attribute] { a => a.name })
	private val localNickField = TextField.contextual(prompt = Some("Link name, optional"))
	private val otherNickField = TextField.contextual(prompt = Some("Link name, optional"))
	
	
	// INITIAL CODE	-----------------------
	
	linkOriginSelection.selectOne(true)
	
	// When link origin changes, so does link type selection display
	linkOriginSelection.addValueListener { _ => linkTypeSelection.updateDisplays() }
	
	// Whenever linked class is changed, link type selection look is updated as well
	classSelection.addValueListener { _ =>
		linkOriginSelection.updateDisplays()
		linkTypeSelection.updateDisplays()
		updateMapKeySelection()
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
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def fields = Vector(
		InputRowInfo("Linked Class", classSelection),
		InputRowInfo("Link starts from", linkOriginSelection),
		InputRowInfo("Link Type", linkTypeSelection),
		InputRowInfo("Map Key", mapKeySelection, rowVisibilityPointer = Some(mapKeyVisibilityPointer)),
		InputRowInfo("Name in this class", localNickField),
		InputRowInfo("Name in linked class", otherNickField)
	)
	
	override protected def produceResult =
	{
		classSelection.value match
		{
			case Some(otherClass) =>
				linkTypeSelection.value match
				{
					case Some(linkType) =>
						val originatesFromThisClass = linkOriginSelection.value.getOrElse(true)
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
						val newLink = NewLinkConfiguration(linkType, originId, targetId, originNick, targetNick,
							mappingKeyAttributeId = mapKeySelection.value.map { _.id })
						if (linkToEdit.exists { _ ~== newLink })
							Right(None)
						else
							Right(Some(newLink))
					case None => Left(linkTypeSelection, "Please specify the link type")
				}
			case None => Left(classSelection, "Please select the linked class")
		}
	}
	
	override protected def defaultResult = None
	
	override protected def title = if (linkToEdit.isDefined) "Edit Link" else "Add Link"
	
	
	// OTHER	---------------------------
	
	private def updateMapKeySelection() =
	{
		if (linkTypeSelection.value.exists { _.usesMapping })
		{
			classSelection.value match
			{
				case Some(linkedClass) =>
					mapKeySelection.content = linkedClass.attributes
					mapKeyVisibilityPointer.value = true
				case None => mapKeyVisibilityPointer.value = false
			}
		}
		else
			mapKeyVisibilityPointer.value = false
	}
}
