package dbd.api.database

import dbd.core.util.ThreadPool
import utopia.vault.model.immutable.Table

/**
  * Used for accessing various tables in DB Designer project (api-side)
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object Tables extends utopia.vault.database.Tables(ConnectionPool)(ThreadPool.executionContext)
{
	// ATTRIBUTES	----------------------
	
	private val dbName = "db_designer"
	
	
	// COMPUTED	--------------------------------
	
	/**
	  * @return Table that contains descriptions of various things
	  */
	def description = apply("description")
	
	/**
	  * @return Table that contains description role enumeration values
	  */
	def descriptionRole = apply("description_role")
	
	/**
	  * @return Table that contains links between description roles and their descriptions
	  */
	def descriptionRoleDescription = apply("description_role_description")
	
	/**
	  * @return Table that contains registered languages
	  */
	def language = apply("language")
	
	/**
	  * @return Table that contains links between languages and their descriptions
	  */
	def languageDescription = apply("language_description")
	
	/**
	  * @return Table that contains users
	  */
	def user = apply("user")
	
	/**
	  * @return Table for user authentication
	  */
	def userAuth = apply("user_authentication")
	
	/**
	  * @return Table that contains device-specific authentication keys
	  */
	def deviceAuthKey = apply("device_authentication_key")
	
	/**
	  * @return Table that contains temporary user session keys
	  */
	def userSession = apply("user_session")
	
	/**
	  * @return Table for user settings
	  */
	def userSettings = apply("user_settings")
	
	/**
	  * @return Table that links users with languages
	  */
	def userLanguage = apply("user_language")
	
	/**
	  * @return Table that registers the devices the clients use
	  */
	def clientDevice = apply("client_device")
	
	/**
	  * @return Table that links users with the devices they are using
	  */
	def userDevice = apply("client_device_user")
	
	/**
	  * @return A table that contains links between devices and their descriptions
	  */
	def deviceDescription = apply("client_device_description")
	
	/**
	  * @return Table that contains organizations
	  */
	def organization = apply("organization")
	
	/**
	  * @return Table that contains links between organizations and their descriptions
	  */
	def organizationDescription = apply("organization_description")
	
	/**
	  * @return Contains attempted and pending organization deletions
	  */
	def organizationDeletion = apply("organization_deletion")
	
	/**
	  * @return Contains organization deletion cancellations
	  */
	def organizationDeletionCancellation = apply("organization_deletion_cancellation")
	
	/**
	  * @return Table that contains organization user memberships
	  */
	def organizationMembership = apply("organization_membership")
	
	/**
	  * @return Table that lists all user roles
	  */
	def userRole = apply("organization_user_role")
	
	/**
	  * @return Table that contains links between user roles and their descriptions
	  */
	def roleDescription = apply("user_role_description")
	
	/**
	  * @return Table that contains role links for organization memberships
	  */
	def organizationMemberRole = apply("organization_member_role")
	
	/**
	  * @return Table that contains links between user roles and the tasks they have access to
	  */
	def roleRight = apply("user_role_right")
	
	/**
	  * @return A table that lists all available tasks/rights
	  */
	def task = apply("task")
	
	/**
	  * @return A table that contains links between tasks and descriptions
	  */
	def taskDescription = apply("task_description")
	
	/**
	  * @return Table that contains sent organization join invitations
	  */
	def organizationInvitation = apply("organization_invitation")
	
	/**
	  * @return Table that contains responses to organization join invitations
	  */
	def invitationResponse = apply("invitation_response")
	
	/**
	  * @return Table that contains registered databases
	  */
	def database = apply("database")
	
	/**
	  * @return Table that contains database configurations
	  */
	def databaseConfiguration = apply("database_configuration")
	
	/**
	  * @return Table that contains classes
	  */
	def classTable = apply("class")
	
	/**
	  * @return Table that contains class attributes
	  */
	def attribute = apply("attribute")
	
	/**
	  * @return Table that contains base class info
	  */
	def classInfo = apply("class_info")
	
	/**
	  * @return Table that contains configurations for class attributes
	  */
	def attributeConfiguration = apply("attribute_configuration")
	
	/**
	  * @return Table that contains links between classes
	  */
	def link = apply("link")
	
	/**
	  * @return Table that contains configurations for links between classes
	  */
	def linkConfiguration = apply("link_configuration")
	
	/**
	  * @return Table that contains database releases
	  */
	def release = apply("database_release")
	
	/**
	  * @return Table that contains released tables
	  */
	def table = apply("table_release")
	
	/**
	  * @return Table that contains released column data
	  */
	def column = apply("column_release")
	
	/**
	  * @return Table that contains links between columns, attributes and indices
	  */
	def columnAttributeLink = apply("column_attribute_link")
	
	/**
	  * @return Table that contains links between columns, links and foreign keys
	  */
	def columnLinkLink = apply("column_link_link")
	
	/**
	  * @return Table that contains released index data
	  */
	def index = apply("index_release")
	
	/**
	  * @return Table that contains released foreign key data
	  */
	def foreignKey = apply("foreign_key_release")
	
	
	// OTHER	-------------------------------
	
	/**
	  * @param tableName Name of targeted table
	  * @return a cached table
	  */
	def apply(tableName: String): Table = apply(dbName, tableName)
}
