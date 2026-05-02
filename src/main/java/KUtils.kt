import burp.api.montoya.persistence.Persistence
import java.io.File

object KUtils {
  @JvmStatic
  fun checkFileExists(path: String?): Boolean {
    if (path.isNullOrEmpty()) return false
    return File(path).isFile
  }

  @JvmStatic
  fun cleanUrl(url: String): String {
    return url.split("?")[0]
  }

  @JvmStatic
  fun getCommandFromPath(persistence: Persistence, path: String?): String? {
    if (path.isNullOrEmpty()) return null

    var globalPython = persistence.preferences().getString(K.KEYS.GLOBAL_PYTHON_PATH)
    var globalNode = persistence.preferences().getString(K.KEYS.GLOBAL_NODE_PATH)

    var nodePath = persistence.extensionData().getString(K.KEYS.PROJECT_NODE_PATH)
    var pythonPath = persistence.extensionData().getString(K.KEYS.PROJECT_PYTHON_PATH)

    if (!checkFileExists(globalPython)) globalPython = null
    if (!checkFileExists(globalNode)) globalNode= null

    if (!checkFileExists(pythonPath)) pythonPath = globalPython
    if (!checkFileExists(nodePath)) nodePath = globalNode

    val dotIndex = path.lastIndexOf(".")

    if (dotIndex > 0) {
      val extension = path.substring(dotIndex + 1)
      return when (extension) {
        "py" -> pythonPath
        "js" -> nodePath
        else -> null
      }
    }

    return null
  }

}
