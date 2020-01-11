package dbd.core.util

/**
 * Used for logging errors
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object Log
{
	/**
	 * Logs an error
	 * @param error Error to log
	 * @param message Additional error message
	 */
	def apply(error: Throwable, message: String) =
	{
		println(message)
		error.printStackTrace()
	}
}
