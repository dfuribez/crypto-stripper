package Utils2

import burp.api.montoya.MontoyaApi
import java.awt.Component
import javax.swing.JFileChooser
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
