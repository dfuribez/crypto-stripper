import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor
import java.awt.Component

class StripperResponseEditor(
  var montoyaApi: MontoyaApi,
  var editorCreationContext: EditorCreationContext
) : ExtensionProvidedHttpResponseEditor {

  var currentRequestResponse: HttpRequestResponse? = null
  lateinit var previewTabGUI: PreviewTabGUI

  init {
    val tool = editorCreationContext.toolSource().toolType().toolName().lowercase()
    previewTabGUI = PreviewTabGUI(montoyaApi, false, tool)
  }

  override fun getResponse(): HttpResponse? {
    return currentRequestResponse?.response()
  }

  override fun setRequestResponse(requestResponse: HttpRequestResponse?) {
    this.currentRequestResponse = requestResponse
    previewTabGUI.setRequestResponse(requestResponse)
    previewTabGUI.setContent(requestResponse?.response())
  }

  override fun isEnabledFor(requestResponse: HttpRequestResponse?): Boolean {
    if (requestResponse == null
      || requestResponse.request() == null) return false

    this.currentRequestResponse = requestResponse
    val url = KUtils.cleanUrl(requestResponse.request().url())
    val scope = Utils.loadScope(montoyaApi.persistence().extensionData())

    previewTabGUI.setRequestResponse(requestResponse)
    return Utils.isUrlInScope(url, scope["scope"])
  }

  override fun caption(): String {
    return "Stripper"
  }

  override fun uiComponent(): Component? {
    return previewTabGUI.mainPanel
  }

  override fun selectedData(): Selection? {
    return null
  }

  override fun isModified(): Boolean {
    return false
  }
}
