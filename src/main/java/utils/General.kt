package utils

import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.Annotations
import burp.api.montoya.core.HighlightColor
import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.persistence.Persistence
import burp.api.montoya.scanner.audit.issues.AuditIssue
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.Font
import java.io.File
import java.io.IOException
import java.net.http.HttpHeaders
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.collections.ArrayList


fun openChooser(
  parent: Component,
  fileChooser: JFileChooser,
  title: String,
  filter: FileNameExtensionFilter?,
  isOpenDialog: Boolean
): String {
  fileChooser.dialogTitle = title
  fileChooser.fileFilter = filter

  val response = if (isOpenDialog) {
    fileChooser.showOpenDialog(parent)
  } else {
    fileChooser.showSaveDialog(parent)
  }

  if (response == JFileChooser.APPROVE_OPTION) {
    return fileChooser.selectedFile.absolutePath
  }

  return ""
}

fun isValidRegex(pattern: String) : Boolean{
  try {
    Regex(pattern)
    return true
  } catch (e: Exception) {
    return false
  }
}

fun openFolder(path: String?): Boolean {
  if (path.isNullOrBlank()) return false
  if (Desktop.isDesktopSupported()) {
    val file = File(path)
    val desktop = Desktop.getDesktop()
    try {
      desktop.open(File(file.parent))
      return true
    } catch (e: IOException) {
      return false
    }
  }
  return false
}

fun showAlertMessage(parent: Component, message: String) {
  JOptionPane.showMessageDialog(parent, message)
}

fun isUrlInScope(url: String, scope: PersistedList<String>): Boolean {
  for (regex in scope) {
    try {
      if (url.matches(regex.toRegex())) {
        return true
      }
    } catch (e: java.lang.Exception) {
      return false
    }
  }
  return false
}

fun printError(montoyaApi: MontoyaApi, source: String, error: String) {
  val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
  montoyaApi.logging().logToError(
    "[+] $now at $source\n\t$error\n"
  )
}

fun setAnnotation(
  currentNote: String?, color: String?, note: String?
): Annotations {
  var currentNote = currentNote
  var note = note
  val annotation = Annotations.annotations()

  currentNote = if (currentNote.isNullOrBlank()) "" else currentNote
  note = if (note.isNullOrBlank()) "" else note
  val separator = if (currentNote.isBlank() || note.isBlank()) "" else ", "

  if (!(color.isNullOrBlank() || color.equals("NONE", ignoreCase = true))
  ) {
    annotation
      .setHighlightColor(
        HighlightColor.highlightColor(color.uppercase(Locale.getDefault()))
      )
  }

  annotation.setNotes(currentNote + separator + note)
  return annotation
}

fun setIssue(
  montoyaApi: MontoyaApi, issue: MutableMap<String?, String?>,
  url: String?, request: HttpRequest?, response: HttpResponse?
) {
  try {
    montoyaApi.siteMap().add(
      AuditIssue.auditIssue(
        issue.get("name"),
        issue.get("detail"),
        issue.get("remediation"),
        url,
        AuditIssueSeverity.INFORMATION,
        AuditIssueConfidence.CERTAIN,
        issue.get("background"),
        issue.get("remediationBackground"),
        AuditIssueSeverity.INFORMATION,
        HttpRequestResponse.httpRequestResponse(request, response)
      )
    )
  } catch (e: java.lang.Exception) {
    montoyaApi.logging().logToError("Error setting issue", e)
  }
}

fun checkScriptVersion(version: Short): Boolean {
  return version >= 2
}

fun resourceToFile(montoyaApi: MontoyaApi, resource: String, path: String): Boolean {
  val inputStream = object {}.javaClass.getResourceAsStream(resource)
  if (inputStream == null) return false

  val ext = resource.split(".", limit=2)
  var withExt = path

  if (!path.endsWith(ext[1], ignoreCase = true)) withExt = "$path.${ext[1]}"

  val outputFile = File(withExt)
  inputStream.use { input ->
    outputFile.outputStream().use { output -> input.copyTo(output) }
  }
  return true
}

fun checkFileExists(path: String?): Boolean {
  if (path.isNullOrEmpty()) return false
  return File(path).isFile
}

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

fun headersToArray(burpHeaders: List<HttpHeader>): ArrayList<String?> {
  val headers = ArrayList<String?>()

  for (header in burpHeaders) {
    if (!K.Gen.dangerousPseudoHeaders.contains(header.name())) {
      headers.add(header.toString())
    }
  }

  return headers
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

fun separator(
  title: String,
  type: String = "center",
  visible: Boolean = true,
  font: Font? = null
): JPanel {
  val separator = JPanel(MigLayout("insets 0"))

  val titleLabel = JLabel(title)
  if (font != null) titleLabel.font = font

  if (type.equals("center", ignoreCase = true)) {
    separator.add(spacer(visible), "growx, pushx")
    separator.add(titleLabel)
    separator.add(spacer(visible), "growx, pushx")
  } else if (type.equals("left", ignoreCase = true)) {
    separator.add(titleLabel)
    separator.add(spacer(visible), "growx, pushx")
  } else {
    separator.add(spacer(visible), "growx, pushx")
    separator.add(titleLabel)
  }

  return separator
}

fun spacer(visible: Boolean): Component {
  if (visible) {
    return JSeparator(SwingConstants.HORIZONTAL)
  }
  return JPanel()
}
