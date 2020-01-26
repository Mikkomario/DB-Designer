package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.client.controller.ClassDisplayManager
import dbd.client.dialog.AddSubClassMode.{AdoptExistingClass, CreateNewSubClass}
import dbd.client.model.{ChildLink, EditSubClassResult}
import dbd.core.model.enumeration.LinkEndRole.{Origin, Target}
import dbd.core.model.enumeration.LinkType
import dbd.core.model.existing.{Attribute, Class}
import dbd.core.model.partial.{NewClassInfo, NewLinkConfiguration, NewSubClass}
import dbd.core.model.template.ClassLike
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.{DropDown, Switch, TabSelection, TextField}
import utopia.reflection.localization.{DisplayFunction, LocalizedString, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.collection.immutable.VectorBuilder
import scala.concurrent.ExecutionContext

/**
 * Used for adding and editing sub-classes
 * @author Mikko Hilpinen
 * @since 26.1.2020, v0.1
 */
class EditSubClassDialog(parent: Class, editedChildLink: Option[ChildLink], classManager: ClassDisplayManager)
						(implicit baseCB: ComponentContextBuilder, colorScheme: ColorScheme, localizer: Localizer,
						 margins: Margins, exc: ExecutionContext)
	extends InputDialog[Option[Either[EditSubClassResult, Either[NewLinkConfiguration, NewSubClass]]]]
{
	// ATTRIBUTES	--------------------------
	
	private implicit val language: String = "en"
	private implicit val baseContext: ComponentContext = baseCB.result
	
	// Used for selecting way of adding new classes (only in new class mode)
	private val createStyleSelection = editedChildLink match
	{
		case Some(_) => None
		case None =>
			Some(TabSelection.contextual(DisplayFunction.functionToDisplayFunction[AddSubClassMode] { _.localizedName },
				AddSubClassMode.values)(baseCB.withColors(colorScheme.primary).result))
	}
	
	// Adopt class selection is displayed only in adopt-mode, otherwise class name field is displayed
	private val adoptedClassSelection = editedChildLink match
	{
		case Some(_) => None
		case None => Some(DropDown.contextual("Select class to adopt",
			DisplayFunction.noLocalization[ClassLike[_, _, _]] { _.name }, classManager.potentialChildrenFor(parent.id)))
	}
	private val classNameField = TextField.contextual(initialText = editedChildLink.map { _.child.name }.getOrElse(""),
		prompt = Some("Name of sub-class"))
	
	// Used for selecting parent-child relationship
	private val linkTypeSelection = DropDown.contextual("Select class relationship", linkTypeDisplayFunction,
		currentLinkOptions)
	
	// Used for selecting child class mutability
	private val isMutableSwitch = Switch.contextual
	
	// Used for selecting mapping key (when applicable)
	private val mapKeySelection = DropDown.contextual[Attribute]("Select mapping key attribute",
		DisplayFunction.noLocalization[Attribute] { _.name })
	
	private val linkNameInParentField = TextField.contextual(initialText = editedChildLink.flatMap { _.nameInOwner }.getOrElse(""),
		prompt = Some("Link Nickname, Optional"))
	private val linkNameInChildField = TextField.contextual(initialText = editedChildLink.flatMap { _.nameInChild }.getOrElse(""),
		prompt = Some("Link Nickname, Optional"))
	
	private val adoptSelectionVisibility = if (adoptedClassSelection.isDefined) Some(new PointerWithEvents(false)) else None
	private val classNameFieldVisibility = if (adoptedClassSelection.isDefined) Some(new PointerWithEvents(true)) else None
	private val mapKeySelectionVisibility = new PointerWithEvents(false)
	
	
	// INITIAL CODE	------------------------
	
	// Create style selection affects field visibility and child name (which affects link type display)
	// Selectable link styles may also change when adopted class changes
	createStyleSelection.foreach { field =>
		field.selectOne(CreateNewSubClass)
		field.addValueListener { change =>
			// Updates field visibility (class name field or child class selection field is visible)
			val newMode = change.newValue.getOrElse(CreateNewSubClass)
			adoptSelectionVisibility.foreach { _.value = newMode == AdoptExistingClass }
			classNameFieldVisibility.foreach { _.value = newMode == CreateNewSubClass }
			
			// Updates link type options
			updateLinkTypeOptions()
		}
	}
	
	// Adopted class affects link type options and map key options
	adoptedClassSelection.foreach { _.addValueListener { event =>
		updateLinkTypeOptions()
		mapKeySelection.content = event.newValue.map { _.attributes }.getOrElse(Vector())
	} }
	
	// Inputted class name affects link type display
	classNameField.addResultListener { _ => linkTypeSelection.updateDisplays() }
	
	// Link type selection affects whether mutability is selectable or not
	// Mutability is allowed only on link types without deprecation or mapping
	// Also, map key selection is displayed only when link type uses mapping
	linkTypeSelection.addValueListener { event =>
		val allowMutability = event.newValue.exists { t => !t.usesDeprecation && !t.usesMapping }
		if (!allowMutability && isMutableSwitch.isOn)
			isMutableSwitch.isOn = false
		isMutableSwitch.isEnabled = allowMutability
		mapKeySelectionVisibility.value = event.newValue.exists { _.usesMapping }
	}
	
	// Sets initial values based on edit
	editedChildLink.foreach { link =>
		mapKeySelection.content = link.child.attributes
		linkTypeSelection.selectOne(link.linkType)
		isMutableSwitch.isOn = link.child.isMutable
		link.mappingKeyAttributeId.foreach { attId => mapKeySelection.selectFirstWhere { _.id == attId } }
	}
	
	
	// COMPUTED	----------------------------
	
	private def currentAddMode: AddSubClassMode = createStyleSelection.flatMap { _.value }.getOrElse(CreateNewSubClass)
	
	private def currentlyAdoptedClass =
	{
		if (currentAddMode == AdoptExistingClass)
			adoptedClassSelection.flatMap { _.value }
		else
			None
	}
	
	// Map relationship can be used when linking to an existing class with attributes
	private def allowsMapRelationship =
	{
		editedChildLink.map { _.child }.orElse(currentlyAdoptedClass).exists {
			_.attributes.nonEmpty }
	}
	
	private def currentLinkOptions =
	{
		val baseList = LinkType.values.filter { _.isOwnable }
		if (allowsMapRelationship)
			baseList
		else
			baseList.filterNot { _.usesMapping }
	}
	
	private def parentName = parent.name
	
	private def childName: String =
	{
		val selectedName = createStyleSelection.flatMap { _.value }.getOrElse(CreateNewSubClass) match
		{
			case CreateNewSubClass => classNameField.value
			case AdoptExistingClass => adoptedClassSelection.flatMap { _.value.map { _.name } }
		}
		selectedName.getOrElse("Sub-Class".autoLocalized.string)
	}
	
	// NB: Display function provides different results based on adoptedClassSelection and classNameField values
	private def linkTypeDisplayFunction =
	{
		DisplayFunction.functionToDisplayFunction[LinkType] { linkType =>
			val base = linkType.nameWithClassSlots.autoLocalized
			linkType.fixedOwner match
			{
				case Origin => base.interpolate(parentName, childName)
				case Target => base.interpolate(childName, parentName)
			}
		}
	}
	
	
	// IMPLEMENTED	----------------------
	
	override protected def fields =
	{
		val builder = new VectorBuilder[InputRowInfo]
		createStyleSelection.foreach { field => builder += InputRowInfo("Operation", field) }
		adoptedClassSelection.foreach { field => builder += InputRowInfo("Class to Adopt", field,
			rowVisibilityPointer = adoptSelectionVisibility) }
		builder += InputRowInfo("Class Name", classNameField, rowVisibilityPointer = classNameFieldVisibility)
		builder += InputRowInfo("Relationship", linkTypeSelection)
		builder += InputRowInfo("Mapping Key", mapKeySelection, rowVisibilityPointer = Some(mapKeySelectionVisibility))
		builder += InputRowInfo("Is Mutable", isMutableSwitch, spansWholeRow = false)
		builder += InputRowInfo("Link name in %s".autoLocalized.interpolate(parent.name), linkNameInParentField)
		builder += InputRowInfo("Link name in sub-class", linkNameInChildField)
		builder.result()
	}
	
	override protected def produceResult =
	{
		// Reads common fields first
		linkTypeSelection.value match
		{
			case Some(linkType) =>
				val mapKeyUsed = linkType.usesMapping
				val mapKey = if (mapKeyUsed) mapKeySelection.value else None
				if (mapKeyUsed && mapKey.isEmpty)
					Left(mapKeySelection, "Please select the attribute to map by")
				else
				{
					val mutability = isMutableSwitch.isOn
					val nameInParent = linkNameInParentField.value
					val nameInChild = linkNameInChildField.value
					val linkNames = Map(linkType.fixedOwner -> nameInParent, linkType.fixedChild -> nameInChild)
					val nameInOrigin = linkNames(Origin)
					val nameInTarget = linkNames(Target)
					
					// Final result depends on the model add style
					currentAddMode match
					{
						case CreateNewSubClass =>
							classNameField.value match
							{
								case Some(className) =>
									val newClassInfo = NewClassInfo(className, mutability)
									editedChildLink match
									{
										case Some(linkToEdit) =>
											// Case: Existing link/child edited
											val updatedClassInfo = if (newClassInfo ~== linkToEdit.child.info)
												None else Some(newClassInfo)
											val (originClassId, targetClassId) =
											{
												if (linkType.fixedOwner == linkToEdit.linkType.fixedOwner)
													linkToEdit.originClassId -> linkToEdit.targetClassId
												else
													linkToEdit.targetClassId -> linkToEdit.originClassId
											}
											val newLinkConfig = NewLinkConfiguration(linkType, originClassId,
												targetClassId, nameInOrigin, nameInTarget, isOwned = true, mapKey.map { _.id })
											val updatedLinkConfig = if (newLinkConfig ~== linkToEdit.configuration)
												None else Some(newLinkConfig)
											Right(Some(Left(EditSubClassResult(updatedLinkConfig, updatedClassInfo))))
										// Case: New sub-class added
										case None => Right(Some(Right(Right(NewSubClass(newClassInfo, linkType, parent.id,
											nameInParent, nameInChild)))))
									}
								case None => Left(classNameField, "Please specify the name of the new class")
							}
						case AdoptExistingClass =>
							adoptedClassSelection match
							{
								case Some(adoptField) =>
									adoptField.value match
									{
										case Some(adoptedClass) =>
											val linkIds = Map(linkType.fixedOwner -> parent.id,
												linkType.fixedChild -> adoptedClass.classId)
											// Case: Existing class adopted
											Right(Some(Right(Left(NewLinkConfiguration(linkType, linkIds(Origin),
												linkIds(Target), nameInOrigin, nameInTarget, isOwned = true,
												mapKey.map { _.id })))))
										case None => Left(adoptField, "Please select the class to adopt")
									}
								case None => Right(None)
							}
					}
				}
			case None => Left(linkTypeSelection, "Please specify class relationship")
		}
	}
	
	override protected def defaultResult = None
	
	override protected def title = if (editedChildLink.isDefined) "Edit Sub-Class" else "Add Sub-Class"
	
	
	// OTHER	--------------------------
	
	private def updateLinkTypeOptions() =
	{
		val newLinkOptions = currentLinkOptions
		if (linkTypeSelection.content == newLinkOptions)
			linkTypeSelection.updateDisplays()
		else
			linkTypeSelection.content = newLinkOptions
	}
}

private sealed trait AddSubClassMode
{
	def localizedName(implicit localizer: Localizer): LocalizedString
}

private object AddSubClassMode
{
	private implicit val language: String = "en"
	
	case object CreateNewSubClass extends AddSubClassMode
	{
		override def localizedName(implicit localizer: Localizer) = "Create New"
	}
	case object AdoptExistingClass extends AddSubClassMode
	{
		override def localizedName(implicit localizer: Localizer) = "Adopt Existing"
	}
	
	val values = Vector(CreateNewSubClass, AdoptExistingClass)
}