import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.Annotations
import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.persistence.Persistence
import jdk.jshell.execution.Util
import models.EditedRequest
import models.EditedResponse
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.io.File
import java.net.URI
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants

object KUtils {

  object Url {
    @JvmStatic
    fun clean(url: String): String {
      return url.split("?")[0]
    }
  }

  object Response {
    fun edit(
      montoyaApi: MontoyaApi,
      response: HttpResponse,
      url: String,
      annotations: Annotations,
      messageId: Int,
      action: String,
      toolName: String
    ) : EditedResponse {
      var newAnnotations: Annotations? = null
      val ready =
        Utils.prepareResponseForExecutor(response, url, messageId, toolName)

      val executed = Executor.execute(montoyaApi, action, "response", ready);
      val response = Utils.executorToHttpResponse(response, executed)

      if (executed.issue != null) {
        Utils.setIssue(
          montoyaApi,
          executed.issue,
          url,
          HttpRequest.httpRequest(),
          response
        )
      }

      if (executed.annotation != null) {
        newAnnotations = Utils.setAnnotation(
          annotations.notes(),
          executed.annotation["color"],
          executed.annotation["note"]
        )
      }

      return EditedResponse(response, newAnnotations ?: annotations)
    }
  }
  object Request {
    @JvmStatic
    fun edit(montoyaApi: MontoyaApi,
             request: HttpRequest,
             annotations: Annotations,
             messageId: Int,
             action: String,
             toolName: String
    ) : EditedRequest {
      val url = KUtils.Url.clean(request.url())
      var newAnnotations: Annotations? = null

      val ready = Utils.prepareRequestForExecutor(request, messageId, toolName)
      val executed = Executor.execute(montoyaApi, action, "request", ready)
      val edited = Utils.executorToHttpRequest(request, executed)

      if (!executed.issue.isNullOrEmpty()) {
        Utils.setIssue(
          montoyaApi,
          executed.issue,
          url,
          request,
          HttpResponse.httpResponse()
        )
      }

      if (!executed.annotation.isNullOrEmpty()) {
        newAnnotations = Utils.setAnnotation(
          annotations.notes(),
          executed.annotation["color"],
          executed.annotation["note"]
        )
      }
      return EditedRequest(edited, newAnnotations ?: annotations, executed.intercept)
    }
  }


  @JvmStatic
  fun checkFileExists(path: String?): Boolean {
    if (path.isNullOrEmpty()) return false
    return File(path).isFile
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

  @JvmStatic
  fun removePath(url: String): String? {
    try {
      val uri = URI(url)
      return URI(uri.scheme, uri.userInfo, uri.host, uri.port,
        null, null, null).toString()
    } catch (e: Exception) {
      return null
    }
  }

  @JvmStatic
  fun headersToArray(burpHeaders: List<HttpHeader>): ArrayList<String?> {
    val headers = ArrayList<String?>()

    for (header in burpHeaders) {
      if (!K.Gen.dangerousPseudoHeaders.contains(header.name())) {
        headers.add(header.toString())
      }
    }

    return headers
  }


  @JvmStatic
  fun separator(title: String, type: String = "center", visible: Boolean = true): JPanel {
    val separator = JPanel(MigLayout("insets 0"))

    if (type.equals("center", ignoreCase = true)) {
      separator.add(spacer(visible), "growx, pushx")
      separator.add(JLabel(title))
      separator.add(spacer(visible), "growx, pushx")
    } else if (type.equals("left", ignoreCase = true)) {
      separator.add(JLabel(title))
      separator.add(spacer(visible), "growx, pushx")
    } else {
      separator.add(spacer(visible), "growx, pushx")
      separator.add(JLabel(title))
    }

    return separator
  }

  fun spacer(visible: Boolean): Component {
    if (visible) {
      return JSeparator(SwingConstants.HORIZONTAL)
    }

    return JPanel()
  }

  fun escapeHtml(input: String): String {
    return input
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#x27;")
      .replace("\n", "<br>")
  }

  fun printError(montoyaApi: MontoyaApi, source: String, error: String) {
    val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    montoyaApi.logging().logToError(
      "[+] $now at $source\n\t$error\n"
    )
  }
}
