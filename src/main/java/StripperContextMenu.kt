import KUtils.Url.clean
import KUtils.removePath
import Utils2.Settings.scope
import Utils2.isUrlInScope
import Utils2.isValidRegex
import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.Annotations
import burp.api.montoya.core.ToolType
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.params.HttpParameter
import burp.api.montoya.http.message.params.HttpParameterType
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.persistence.PersistedList
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import burp.api.montoya.ui.contextmenu.InvocationType
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse
import java.awt.Component
import burp.api.montoya.core.ByteArray;
import java.awt.Font
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import javax.swing.JLabel
import javax.swing.JMenuItem


class StripperContextMenu(
  var montoyaApi: MontoyaApi,
  var stripperTab: StripperTab
) : ContextMenuItemsProvider {
  var insertDialog: PayloadsGUI

  init {
    insertDialog = PayloadsGUI(montoyaApi)
  }

  private fun decryptRequest(
    requestResponse: MessageEditorHttpRequestResponse?,
    tool: String
  ) {
    if (requestResponse == null) return
    val request = requestResponse.requestResponse().request()
    val annotations = Annotations.annotations()
    val edited = KUtils.Request.edit(montoyaApi, request, annotations, -1, "decrypt", tool)
    try {
      requestResponse.setRequest(edited.request)
    } catch (e: Exception) {

    }
  }

  override fun provideMenuItems(event: ContextMenuEvent): List<Component> {
    val menuItemList = ArrayList<Component>()

    val f = JLabel().font
    val separatorFont = Font(f.name, f.style, (f.size * 0.7).toInt())

    var requestResponse: HttpRequestResponse? = null
    val editorHttpRequestResponse: MessageEditorHttpRequestResponse?

    if (event.messageEditorRequestResponse().isPresent) {
      requestResponse = event.messageEditorRequestResponse().get().requestResponse()
      editorHttpRequestResponse = event.messageEditorRequestResponse().get()
    } else {
      editorHttpRequestResponse = null
      requestResponse = event.selectedRequestResponses().first()
    }

    val url = clean(requestResponse.request().url())
    val scope = scope(montoyaApi)
    val source = event.toolType().toolName().lowercase()

    var insertPayloadMenu: JMenuItem? = null
    var decryptMenu: JMenuItem? = null
    var stripperScopeMenu: JMenuItem? = null
    var stripperBlackListMenu: JMenuItem? = null
    var stripperForceMenu: JMenuItem? = null
    var addToPassThroughMenu: JMenuItem? = null
    var burpScopeMenu: JMenuItem? = null

    val isEditable = event.isFromTool(ToolType.PROXY, ToolType.REPEATER)
    val isVisible = event.isFromTool(
      ToolType.PROXY, ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)

    if (isEditable && editorHttpRequestResponse != null) {

      if (isUrlInScope(url, scope.scope)
        && !event.isFrom(InvocationType.MESSAGE_VIEWER_REQUEST)
        && !event.isFrom(InvocationType.MESSAGE_VIEWER_RESPONSE)
      ) {
        decryptMenu = JMenuItem("Decrypt")
        decryptMenu.addActionListener { this.decryptRequest(editorHttpRequestResponse, source) }
        menuItemList.add(decryptMenu)
      }
      // Payloads
    }

    if (isVisible) {
      if (isUrlInScope(url, scope.scope)) {
        stripperScopeMenu = JMenuItem("Remove from scope")
        stripperScopeMenu.addActionListener { this.updateScope("scope", "remove", url) }
      } else {
        stripperScopeMenu = JMenuItem("Add url to scope")
        stripperScopeMenu.addActionListener {
          this.updateScope("scope", "add", url)
          this.decryptRequest(editorHttpRequestResponse, source)
        }
      }

      if (isUrlInScope(url, scope.black)) {
        stripperBlackListMenu = JMenuItem("Remove endpoint from blacklist")
        stripperBlackListMenu.addActionListener { this.updateScope("blacklist", "remove", url) }
      } else {
        stripperBlackListMenu = JMenuItem("Add endpoint to blacklist")
        stripperBlackListMenu.addActionListener { this.updateScope("blacklist", "add", url) }
      }

      if (isUrlInScope(url, scope.force)) {
        stripperForceMenu = JMenuItem("Do not force interception")
        stripperForceMenu.addActionListener { this.updateScope("force", "remove", url) }
      } else {
        stripperForceMenu = JMenuItem("Force interception this endpoint")
        stripperForceMenu.addActionListener { this.updateScope("force", "add", url) }
      }

      if (montoyaApi.scope().isInScope(url)) {
        burpScopeMenu = JMenuItem("Exclude URL from Burp's scope")
        burpScopeMenu.addActionListener { montoyaApi.scope().excludeFromScope(removePath(url)) }
      } else {
        burpScopeMenu = JMenuItem("Include URL to Burp's scope")
        burpScopeMenu.addActionListener { montoyaApi.scope().includeInScope(removePath(url)) }
      }

      menuItemList.add(KUtils.separator(" Stripper scope", font = separatorFont, visible = false, type = "left"))
      menuItemList.add(stripperScopeMenu)
      menuItemList.add(stripperBlackListMenu)
      menuItemList.add(stripperForceMenu)

    }

      menuItemList.add(KUtils.separator(" Burp scope", font = separatorFont, visible = false, type = "left"))
      menuItemList.add(burpScopeMenu!!)

      menuItemList.add(KUtils.separator(" Extra", font = separatorFont, visible = false, type = "left"))
      //menuItemList.add(insertPayloadMenu!!)
      return menuItemList
  }


  fun updateScope(source: String, action: String?, url: String) {
    var url = url
    val target: PersistedList<String>
    val key: String

    val scope = scope(montoyaApi)
    if (!isValidRegex(url)) url = Pattern.quote(url)

    when (source) {
      "blacklist" -> {
        target = scope.black
        key = K.KEYS.BLACK_LIST
      }
      "force" -> {
        target = scope.force
        key = K.KEYS.FORCE_INTERCEPT_LIST
      }
      "scope" -> {
        target = scope.scope
        key = K.KEYS.SCOPE_LIST
      }
      else -> return
    }

    if ("add" == action) {
      target.add(url)
    } else {
      target.remove(url)
    }

    montoyaApi.persistence().extensionData().setStringList(key, target)
    stripperTab.loadSettings()
  }

}