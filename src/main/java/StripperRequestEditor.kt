import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.ui.Selection
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import java.awt.Component

class StripperRequestEditor(
  var montoyaApi: MontoyaApi,
  var creationContext: EditorCreationContext
): ExtensionProvidedHttpRequestEditor {

  var currentRequestResponse: HttpRequestResponse? = null

  val previewTabGUI: PreviewTabGUI

  init {
    val source = creationContext.toolSource().toolType().toolName().lowercase()
    previewTabGUI = PreviewTabGUI(montoyaApi, true, source)
  }

  override fun getRequest(): HttpRequest? {
    return currentRequestResponse?.request()
  }

  override fun setRequestResponse(requestResponse: HttpRequestResponse?) {
    this.currentRequestResponse = requestResponse
    previewTabGUI.setRequestResponse(requestResponse)
    previewTabGUI.setContent(requestResponse?.request())
  }

  override fun isEnabledFor(requestResponse: HttpRequestResponse?): Boolean {
    if (requestResponse == null) return false
    try {
      val url = KUtils.Url.clean(requestResponse.request().url())
      val scope = Utils.loadScope(montoyaApi.persistence().extensionData())
      this.currentRequestResponse = requestResponse
      previewTabGUI.setRequestResponse(requestResponse)

      return Utils.isUrlInScope(url, scope["scope"])
          && !creationContext.toolSource().toolType().toolName().equals("Extensions", ignoreCase = true)

    } catch (e: Exception){
      return false;
    }
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
