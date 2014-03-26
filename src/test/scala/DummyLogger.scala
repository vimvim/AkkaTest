import akka.event.LoggingAdapter

/**
 * Created by vim on 3/26/14.
 */
class DummyLogger extends LoggingAdapter {

  override protected def notifyDebug(message: String): Unit = {}

  override protected def notifyInfo(message: String): Unit = {}

  override protected def notifyWarning(message: String): Unit = {}

  override protected def notifyError(cause: Throwable, message: String): Unit = {}

  override protected def notifyError(message: String): Unit = {}

  override def isDebugEnabled: Boolean = false

  override def isInfoEnabled: Boolean = false

  override def isWarningEnabled: Boolean = false

  override def isErrorEnabled: Boolean = false
}
