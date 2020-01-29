package dbd.mysql.model.template

/**
 * Common trait for models that link columns and links
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
trait ColumnLinkLinkLike[FK <: ForeignKeyLike]
{
	/**
	 * @return Id of associated link
	 */
	def linkConfigurationId: Int
	/**
	 * @return Associated foreign key data
	 */
	def foreignKey: FK
}
