package dbd.core.model.existing

import scala.language.implicitConversions

object Stored
{
	implicit def autoAccessData[D](s: Stored[D]): D = s.data
}

/**
  * A common trait for data that has been stored to database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
trait Stored[+Data]
{
	/**
	  * @return This stored instance's row id
	  */
	def id: Int
	
	/**
	  * @return Data contained within this instance
	  */
	def data: Data
}
