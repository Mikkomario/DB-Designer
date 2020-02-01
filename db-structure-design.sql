--
-- DB Structure for DB Designer
--

CREATE DATABASE db_designer;
USE db_designer;

-- Database practically represents a group of classes
CREATE TABLE `database`
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- A mutating configuration for a database
CREATE TABLE database_configuration
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	name VARCHAR(64) NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deprecated_after DATETIME, 
	
	FOREIGN KEY dc_d_link_to_parent_database (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Lists databases last selected by users (probably should only be stored locally)
CREATE TABLE database_selection
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	selected_database_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	
	INDEX (created), 
	
	FOREIGN KEY ds_d_link_to_selected_db (selected_database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `class`
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deleted_after DATETIME, 
	
	INDEX (deleted_after), 
	
	FOREIGN KEY c_d_link_to_owner_db (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE

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
	
	INDEX ci_current_for_class (class_id, deprecated_after), 
	
	FOREIGN KEY ci_master_class_link (class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE
	
)Engine=InnoDB DEFAULT CHARSET=latin1;

-- A single attribute for a class
CREATE TABLE attribute
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	class_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deleted_after DATETIME, 
	
	INDEX (deleted_after), 
	
	FOREIGN KEY a_attribute_owner_class (class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE

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
	
	INDEX ac_current_for_attribute (attribute_id, deprecated_after), 
	
	FOREIGN KEY ac_master_attribute_link (attribute_id) 
		REFERENCES attribute(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- A link between two classes
CREATE TABLE link
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	deleted_after DATETIME, 
	
	INDEX (deleted_after), 
	
	FOREIGN KEY l_d_link_to_owner_db (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE

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
	
	INDEX (deprecated_after), 
	
	FOREIGN KEY lc_l_described_link (link_id) 
		REFERENCES link(id) ON DELETE CASCADE, 
		
	FOREIGN KEY lc_c_link_to_origin_class (origin_class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE, 
		
	FOREIGN KEY lc_c_link_to_target_class (target_class_id) 
		REFERENCES `class`(id) ON DELETE CASCADE, 
		
	FOREIGN KEY lc_a_link_to_mapping_key (mapping_attribute_id) 
		REFERENCES attribute(id) ON DELETE CASCADE

)Engine=InnoDB DEFAULT CHARSET=latin1;

-- Database version updates
CREATE TABLE database_release
(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
	database_id INT NOT NULL, 
	version_number VARCHAR(32) NOT NULL, 
	created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	
	INDEX (created), 
	
	FOREIGN KEY dr_d_updated_database (database_id) 
		REFERENCES `database`(id) ON DELETE CASCADE
	
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