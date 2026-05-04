package Utils2

import burp.api.montoya.persistence.PersistedList
import java.awt.Component
import java.awt.Desktop
import java.io.File
import java.io.IOException
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

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
