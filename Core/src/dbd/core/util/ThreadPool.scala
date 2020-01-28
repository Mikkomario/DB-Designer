package dbd.core.util

/**
 * The thread pool used in DB Designer project
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
object ThreadPool extends utopia.flow.async.ThreadPool("DB Designer")