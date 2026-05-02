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

    val mainTab = MainTabGUI(montoyaApi)

    montoyaApi.extension().setName("Crypto Striper")
    montoyaApi.logging().logToOutput(Constants.VERSION)

    montoyaApi.userInterface().registerSuiteTab("Stripper", mainTab.mainPanel)
    montoyaApi.userInterface().registerContextMenuItemsProvider(
      MyContextMenus(montoyaApi, mainTab))

    montoyaApi.userInterface().registerHttpRequestEditorProvider(this)
    montoyaApi.userInterface().registerHttpResponseEditorProvider(this)

    montoyaApi.http().registerHttpHandler(StripperHttpHandler(montoyaApi, mainTab))
    montoyaApi.proxy().registerRequestHandler(ProxyHttpRequestHandler(montoyaApi, mainTab))
    montoyaApi.proxy().registerResponseHandler(ProxyHttpResponseHandler(montoyaApi, mainTab))
  }

  override fun provideHttpResponseEditor(p0: EditorCreationContext?): ExtensionProvidedHttpResponseEditor? {
    return StripperResponseEditor(montoyaApi, p0)
  }

  override fun provideHttpRequestEditor(p0: EditorCreationContext?): ExtensionProvidedHttpRequestEditor? {
    return StripperRequestEditor(montoyaApi, p0)
  }
}
