package dbd.mysql.model.template

/**
 * A common trait for models that link columns and attributes
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
trait ColumnAttributeLinkLike[+Index]
{
	/**
	 * @return Id of the associated attribute
	 */
	def attributeConfigurationId: Int
	/**
	 * @return A possible index linked to this column
	 */
	def index: Option[Index]
}
