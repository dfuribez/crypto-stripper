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
import java.awt.Font
import java.io.File
import java.net.URI
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants

object KUtils {
  @JvmStatic
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
}
