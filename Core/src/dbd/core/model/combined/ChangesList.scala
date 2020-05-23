package dbd.core.model.combined

import java.time.Instant

import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Used for listing changes after a specific time threshold
  * @author Mikko Hilpinen
  * @since 23.5.2020, v2
  */
case class ChangesList[+A <: ModelConvertible](added: Vector[A], modified: Vector[A], deleted: Vector[A],
											   timestamp: Instant) extends ModelConvertible
{
	override def toModel = Model(Vector("timestamp" -> timestamp, "added" -> added.map { _.toModel },
			"modified" -> modified.map { _.toModel }, "deleted" -> deleted.map { _.toModel }))
}
