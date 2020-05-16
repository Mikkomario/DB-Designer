--
-- DB Structure for DB Designer
--

CREATE DATABASE db_designer;
USE db_designer;

-- Various languages
CREATE TABLE `language`
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    iso_code VARCHAR(2) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX (iso_code)

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- 1 = English
INSERT INTO `language` (id, iso_code) VALUES (1, 'en');

-- Describes individual users
CREATE TABLE `user`
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Versioned settings for users
CREATE TABLE user_settings
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX (name),
    INDEX (email),
    INDEX (deprecated_after),

    FOREIGN KEY us_u_described_user (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Contains hashed user passwords
-- TODO: Set correct hash length
CREATE TABLE user_authentication
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    hash VARCHAR(128) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX ua_user_password_idx (hash, user_id),

    FOREIGN KEY ua_u_link_to_owner (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Links user's known languages to the user
CREATE TABLE user_language
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    language_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY ul_u_described_user (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE,

    FOREIGN KEY ul_l_known_language (language_id)
        REFERENCES `language`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Enumeration for various description roles (label, use documentation etc.)
CREATE TABLE description_role
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- 1 = Name
INSERT INTO description_role (id) VALUES (1);

-- Descriptions describe various things.
-- Descriptions can be overwritten and are written in a specific language.
-- Usually there is only up to one description available for each item, per language.
CREATE TABLE description
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    language_id INT NOT NULL,
    `text` VARCHAR(255) NOT NULL,
    author_id INT,
    created TIMESTAMP NOT NULl DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY d_dr_description_purpose (role_id)
        REFERENCES description_role(id) ON DELETE CASCADE,

    FOREIGN KEY d_l_used_language (language_id)
        REFERENCES `language`(id) ON DELETE CASCADE,

    FOREIGN KEY d_u_description_writer (author_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Links descriptions with languages
CREATE TABLE language_description
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    language_id INT NOT NULL,
    description_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX (deprecated_after),

    FOREIGN KEY ld_l_described_language (language_id)
        REFERENCES `language`(id) ON DELETE CASCADE,

    FOREIGN KEY ld_d_description_for_language (description_id)
        REFERENCES description(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Organizations represent user groups (Eg. company)
CREATE TABLE organization
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator_id INT,

    FOREIGN KEY o_u_organization_founder (creator_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Names & descriptions for organizations
CREATE TABLE organization_description
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    organization_id INT NOT NULL,
    description_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX (deprecated_after),

    FOREIGN KEY od_o_described_organization (organization_id)
        REFERENCES organization(id) ON DELETE CASCADE,

    FOREIGN KEY od_d_description_for_organization (description_id)
        REFERENCES description(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Represent various tasks or features organization members can perform
CREATE TABLE task
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- 1 = Delete organization
-- 2 = Change user roles (to similar or lower)
-- 3 = Invite new users to organization (with similar or lower role)
-- 4 = Edit organization description (including name)
-- 5 = Remove users (of lower role) from the organization
INSERT INTO task (id) VALUES (1), (2), (3), (4), (5);

-- Names and descriptions of various tasks
CREATE TABLE task_description
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    task_id INT NOT NULL,
    description_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX (deprecated_after),

    FOREIGN KEY td_t_described_task (task_id)
        REFERENCES task(id) ON DELETE CASCADE,

    FOREIGN KEY td_d_description_for_task (description_id)
        REFERENCES description(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- An enumeration for various roles within an organization. One user may have multiple roles within an organization.
CREATE TABLE organization_user_role
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- 1 = Owner (all rights)
-- 2 = Admin/Steward (all rights except owner-specific rights)
-- 3 = Manager (rights to modify users)
-- 4 = Developer (rights to create & edit resources and to publish)
-- 5 = Publisher (Read access to data + publish rights)
-- 5 = Reader (Read only access to data)
INSERT INTO organization_user_role (id) VALUES (1), (2);

-- Links to descriptions of user roles
CREATE TABLE user_role_description
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    description_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX (deprecated_after),

    FOREIGN KEY urd_our_described_role (role_id)
        REFERENCES organization_user_role(id) ON DELETE CASCADE,

    FOREIGN KEY urd_d_description_for_role (description_id)
        REFERENCES description(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Links user roles to one or more tasks the users in that role are allowed to perform
CREATE TABLE user_role_right
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    task_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY urr_our_owner_role (role_id)
        REFERENCES organization_user_role(id) ON DELETE CASCADE,

    FOREIGN KEY urr_t_right (task_id)
        REFERENCES task(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO user_role_right (role_id, task_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
    (2, 2), (2, 3), (2, 4), (2, 5);

-- Contains links between users and organizations (many-to-many)
-- One user may belong to multiple organizations and one organization may contain multiple users
CREATE TABLE organization_membership
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    organization_id INT NOT NULL,
    user_id INT NOT NULL,
    started TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended DATETIME,
    creator_id INT,

    INDEX (ended),

    FOREIGN KEY om_o_parent_organization (organization_id)
        REFERENCES organization(id) ON DELETE CASCADE,

    FOREIGN KEY om_u_member (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE,

    FOREIGN KEY om_u_membership_adder (creator_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Links organization members with their roles in the organizations
CREATE TABLE organization_member_role
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    membership_id INT NOT NULL,
    role_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,
    creator_id INT,

    INDEX (deprecated_after),

    FOREIGN KEY omr_described_membership (membership_id)
        REFERENCES organization_membership(id) ON DELETE CASCADE,

    FOREIGN KEY omr_our_member_role (role_id)
        REFERENCES organization_user_role(id) ON DELETE CASCADE,

    FOREIGN KEY omr_u_role_adder (creator_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;


-- Contains invitations for joining an organization
CREATE TABLE organization_invitation
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    organization_id INT NOT NULL,
    recipient_id INT,
    recipient_email VARCHAR(128),
    starting_role_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_in DATETIME NOT NULL,
    creator_id INT,

    INDEX oi_active_invitations_idx (expires_in, recipient_email),

    FOREIGN KEY oi_o_target_organization (organization_id)
        REFERENCES organization(id) ON DELETE CASCADE,

    FOREIGN KEY oi_u_invited_user (recipient_id)
        REFERENCES `user`(id) ON DELETE CASCADE,

    FOREIGN KEY oi_our_initial_role (starting_role_id)
        REFERENCES organization_user_role(id) ON DELETE CASCADE,

    FOREIGN KEY oi_u_inviter (creator_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Registered responses (yes|no) to organization invitations
CREATE TABLE invitation_response
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    invitation_id INT NOT NULL,
    was_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    was_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator_id INT NOT NULL,

    FOREIGN KEY ir_oi_opened_invitation (invitation_id)
        REFERENCES organization_invitation(id) ON DELETE CASCADE,

    FOREIGN KEY ir_u_recipient (creator_id)
        REFERENCES `user`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;


-- Requested deletions for an organization
-- There is a period of time during which organization owners may cancel the deletion
CREATE TABLE organization_deletion
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    organization_id INT NOT NULL,
    creator_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualization DATETIME NOT NULL,

    INDEX (actualization),

    FOREIGN KEY od_o_deletion_target (organization_id)
        REFERENCES organization(id) ON DELETE CASCADE,

    FOREIGN KEY od_u_deletion_proposer (creator_id)
        REFERENCES `user`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE organization_deletion_cancellation
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    deletion_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator_id INT,

    FOREIGN KEY odc_od_cancelled_deletion (deletion_id)
        REFERENCES organization_deletion(id) ON DELETE CASCADE,

    FOREIGN KEY odc_u_cancel_author (creator_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;


-- Devices the users use to log in and use this service
CREATE TABLE client_device
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator_id INT,

    FOREIGN KEY cd_u_first_device_user (creator_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Names and descriptions of client devices
CREATE TABLE client_device_description
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    device_id INT NOT NULL,
    description_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX (deprecated_after),

    FOREIGN KEY cdd_cd_described_device (device_id)
        REFERENCES client_device(id) ON DELETE CASCADE,

    FOREIGN KEY cdd_d_description_for_device (description_id)
        REFERENCES description(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Links users with the devices they have used
CREATE TABLE client_device_user
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    device_id INT NOT NULL,
    user_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX (deprecated_after),

    FOREIGN KEY cdu_cd_used_client_device (device_id)
        REFERENCES client_device(id) ON DELETE CASCADE,

    FOREIGN KEY cdu_u_device_user (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Registers temporary password reset requests, along with their reset codes (these are sent via email)
CREATE TABLE password_reset_request
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    device_id INT NOT NULL,
    reset_code VARCHAR(6) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_in DATETIME NOT NULL,

    INDEX (expires_in),

    FOREIGN KEY prr_u_password_owner (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE,

    FOREIGN KEY prr_cd_source_device (device_id)
        REFERENCES client_device(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Used for allowing a private device access to user account
CREATE TABLE device_authentication_key
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    device_id INT NOT NULL,
    `key` VARCHAR(36) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deprecated_after DATETIME,

    INDEX dak_active_key (`key`, deprecated_after),

    FOREIGN KEY dak_u_key_owner (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE,

    FOREIGN KEY dak_cd_connected_device (device_id)
        REFERENCES client_device(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Represents a temporary login session
CREATE TABLE user_session
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    device_id INT NOT NULL,
    `key` VARCHAR(36) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_in DATETIME NOT NULL,
    logout_time DATETIME,

    INDEX us_active_key (`key`, expires_in, logout_time),

    FOREIGN KEY us_u_session_owner (user_id)
        REFERENCES `user`(id) ON DELETE CASCADE,

    FOREIGN KEY us_cd_session_device (device_id)
        REFERENCES client_device(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Database practically represents a group of classes
CREATE TABLE `database`
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	owner_id INT NOT NULL,
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	deleted_after DATETIME,
	creator_id INT,

	INDEX (deleted_after),

	FOREIGN KEY d_o_owner_organization (owner_id)
	    REFERENCES organization(id) ON DELETE CASCADE,

	FOREIGN KEY d_u_database_creator (creator_id)
	    REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- A mutating configuration for a database
CREATE TABLE database_configuration
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	name VARCHAR(64) NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deprecated_after DATETIME,
	creator_id INT,

	INDEX (deprecated_after),

	FOREIGN KEY dc_d_link_to_parent_database (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE,

    FOREIGN KEY dc_u_last_editor (creator_id)
        REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Lists databases last selected by users (probably should only be stored locally)
-- TODO: Remove this (don't import to server)
CREATE TABLE database_selection
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	user_id INT NOT NULL,
	selected_database_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	
	INDEX (created), 

	FOREIGN KEY ds_u_database_selector (user_id)
	    REFERENCES `user`(id) ON DELETE CASCADE,

	FOREIGN KEY ds_d_link_to_selected_db (selected_database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `class`
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deleted_after DATETIME,
	creator_id INT,
	
	INDEX (deleted_after), 
	INDEX (created), 
	
	FOREIGN KEY c_d_link_to_owner_db (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE,

	FOREIGN KEY c_u_class_creator (creator_id)
	    REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB, DEFAULT CHARSET=latin1;

-- Current settings for class
CREATE TABLE class_info
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	class_id INT NOT NULL, 
	name VARCHAR(96) NOT NULL, 
	is_mutable BOOLEAN NOT NULL DEFAULT FALSE, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deprecated_after DATETIME,
	creator_id INT,
	
	INDEX ci_current_for_class (class_id, deprecated_after), 
	
	FOREIGN KEY ci_master_class_link (class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE,

	FOREIGN KEY ci_u_last_editor (creator_id)
	    REFERENCES `user`(id) ON DELETE SET NULL
	
)Engine=InnoDB DEFAULT CHARSET=latin1;

-- A single attribute for a class
CREATE TABLE attribute
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	class_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deleted_after DATETIME,
	creator_id INT,
	
	INDEX (deleted_after), 
	
	FOREIGN KEY a_attribute_owner_class (class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE,

	FOREIGN KEY a_u_attribute_creator (creator_id)
	    REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Current settings for an attribute
CREATE TABLE attribute_configuration
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	attribute_id INT NOT NULL, 
	name VARCHAR(96) NOT NULL, 
	data_type INT NOT NULL, 
	is_optional BOOLEAN NOT NULL DEFAULT FALSE, 
	is_search_key BOOLEAN NOT NULL DEFAULT FALSE, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deprecated_after DATETIME,
	creator_id INT,
	
	INDEX ac_current_for_attribute (attribute_id, deprecated_after), 
	
	FOREIGN KEY ac_master_attribute_link (attribute_id) 
		REFERENCES attribute(id) ON DELETE CASCADE,

	FOREIGN KEY ac_u_last_editor (creator_id)
	    REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- A link between two classes
CREATE TABLE link
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deleted_after DATETIME,
	creator_id INT,
	
	INDEX (deleted_after), 
	
	FOREIGN KEY l_d_link_to_owner_db (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE,

	FOREIGN KEY l_u_link_creator (creator_id)
	    REFERENCES `user`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Current configuration for a link
CREATE TABLE link_configuration
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	link_id INT NOT NULL, 
	link_type INT NOT NULL, 
	origin_class_id INT NOT NULL, 
	target_class_id INT NOT NULL, 
	name_in_origin VARCHAR(96), 
	name_in_target VARCHAR(96), 
	is_owned BOOLEAN NOT NULL DEFAULT FALSE, 
	mapping_attribute_id INT, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deprecated_after DATETIME,
	creator_id INT,
	
	INDEX (deprecated_after), 
	
	FOREIGN KEY lc_l_described_link (link_id) 
		REFERENCES link(id) ON DELETE CASCADE, 
		
	FOREIGN KEY lc_c_link_to_origin_class (origin_class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE, 
		
	FOREIGN KEY lc_c_link_to_target_class (target_class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE, 
		
	FOREIGN KEY lc_a_link_to_mapping_key (mapping_attribute_id) 
		REFERENCES attribute(id) ON DELETE CASCADE,

	FOREIGN KEY lc_u_last_link_editor (creator_id)
	    REFERENCES `user`(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Database version updates
CREATE TABLE database_release
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	version_number VARCHAR(32) NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	creator_id INT,
	
	INDEX (created), 
	
	FOREIGN KEY dr_d_updated_database (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE,

	FOREIGN KEY dr_u_publisher (creator_id)
	    REFERENCES `user`(id) ON DELETE SET NULL
	
)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Table version updates (tied to db version updates)
CREATE TABLE table_release
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	release_id INT NOT NULL, 
	class_id INT NOT NULL, 
	name VARCHAR(64) NOT NULL, 
	uses_deprecation BOOLEAN NOT NULL DEFAULT FALSE, 
	allows_updates BOOLEAN NOT NULL DEFAULT FALSE,
	
	FOREIGN KEY tr_dr_parent_release (release_id) 
		REFERENCES database_release(id) ON DELETE CASCADE, 
		
	FOREIGN KEY tr_c_data_source_class (class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE
	
)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Column version updates (tied to table version updates)
CREATE TABLE column_release
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	table_id INT NOT NULL, 
	name VARCHAR(64) NOT NULL, 
	data_type INT NOT NULL, 
	allows_null BOOLEAN NOT NULL DEFAULT FALSE, 
	
	FOREIGN KEY cr_tr_link_to_parent_table (table_id) 
		REFERENCES table_release(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Table indices
CREATE TABLE index_release
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	name VARCHAR(64) NOT NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Links between columns and attributes & indices
CREATE TABLE column_attribute_link
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	column_id INT NOT NULL, 
	attribute_configuration_id INT NOT NULL, 
	index_id INT, 
	
	FOREIGN KEY cal_cr_link_to_parent_column (column_id) 
		REFERENCES column_release(id) ON DELETE CASCADE, 
		
	FOREIGN KEY cal_a_data_source_attribute_config (attribute_configuration_id) 
		REFERENCES attribute_configuration(id) ON DELETE CASCADE, 
		
	FOREIGN KEY cal_ir_generated_index (index_id) 
		REFERENCES index_release(id) ON DELETE SET NULL

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Foreign keys between tables
CREATE TABLE foreign_key_release
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	target_table_id INT NOT NULL, 
	base_name VARCHAR(64) NOT NULL, 
	
	FOREIGN KEY fkr_tr_target_table (target_table_id) 
		REFERENCES table_release(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Links between columns, foreign keys and links
CREATE TABLE column_link_link
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	column_id INT NOT NULL, 
	link_configuration_id INT NOT NULL, 
	foreign_key_id INT NOT NULL, 
	
	FOREIGN KEY cll_cr_link_to_parent_column (column_id) 
		REFERENCES column_release(id) ON DELETE CASCADE, 
		
	FOREIGN KEY cll_l_data_source_link_config (link_configuration_id) 
		REFERENCES link_configuration(id) ON DELETE CASCADE, 
		
	FOREIGN KEY cll_fkr_generated_foreign_key (foreign_key_id) 
		REFERENCES foreign_key_release(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;