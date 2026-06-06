import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.ui.editor.extension.EditorCreationContext
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider
import burp.api.montoya.ui.editor.extension.HttpResponseEditorProvider

class Extension : BurpExtension,
  HttpResponseEditorProvider,
  HttpRequestEditorProvider
{
  lateinit var montoyaApi: MontoyaApi
  override fun initialize(montoyaApi: MontoyaApi?) {
    if (montoyaApi == null) return
    this.montoyaApi = montoyaApi

    val stripperTab = StripperTab(montoyaApi)

    montoyaApi.extension().setName("Crypto Striper")
    montoyaApi.logging().logToOutput(K.Gen.VERSION.trimIndent())

    montoyaApi.userInterface().registerSuiteTab("Stripper", stripperTab.getMainPanel())
    montoyaApi.userInterface().registerContextMenuItemsProvider(StripperContextMenu(montoyaApi, stripperTab))

    montoyaApi.userInterface().registerHttpRequestEditorProvider(this)
    montoyaApi.userInterface().registerHttpResponseEditorProvider(this)

    montoyaApi.http().registerHttpHandler(StripperHttpHandler(montoyaApi))
    montoyaApi.proxy().registerRequestHandler(StripperProxyRequestHandler(montoyaApi))
    montoyaApi.proxy().registerResponseHandler(StripperProxyResponseHandler(montoyaApi))
  }

  override fun provideHttpResponseEditor(p0: EditorCreationContext?): ExtensionProvidedHttpResponseEditor? {
    if (p0 == null) return null
    return StripperResponseEditor(montoyaApi, p0)
  }

  override fun provideHttpRequestEditor(p0: EditorCreationContext?): ExtensionProvidedHttpRequestEditor? {
    if (p0 == null) return null
    return StripperRequestEditor(montoyaApi, p0)
  }
}
