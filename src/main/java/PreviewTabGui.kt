import KUtils.getCommandFromPath
import KUtils.separator
import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import burp.api.montoya.ui.editor.EditorOptions
import net.miginfocom.swing.MigLayout
import javax.swing.*

class PreviewTabGui(
  var montoyaApi: MontoyaApi,
  var isRequest: Boolean,
  var toolSource: String
) {
  private val testDecryptionButton = JButton("Decrypt")
  private val testEncryptionButton = JButton("Encrypt")
  private val openScriptButton = JButton("Open")

  var mainPanel: JPanel = JPanel(MigLayout("insets 0"))

  private val stdErrTextPane = JTextPane()
  private val requestIdTextField = JTextField(4)

  private val infoEditorPane = JEditorPane()

  private val toolCombo = JComboBox<String?>(K.Gen.TOOLS)

  private val stdErrScroll = JScrollPane(stdErrTextPane)

  private var requestResponse: HttpRequestResponse? = null

  private var responseEditor = montoyaApi.userInterface().createHttpResponseEditor()
  private var responseTransformed = montoyaApi.userInterface().createHttpResponseEditor(
    EditorOptions.READ_ONLY)

  private var requestEditor = montoyaApi.userInterface().createHttpRequestEditor()
  private var requestTransformed = montoyaApi.userInterface().createHttpRequestEditor(
    EditorOptions.READ_ONLY)

  private var scriptPath: String? = null


  init {
    initialize()

    stdErrTextPane.contentType = "text/html"

    testEncryptionButton.addActionListener { execute("encrypt") }
    testDecryptionButton.addActionListener { execute("decrypt") }

    openScriptButton.addActionListener { Utils.openFolder(scriptPath) }

    layout()
  }

  private fun initialize() {
    testDecryptionButton.setBackground(K.Color.MAIN_BUTTON_BACKGROUND)
    testEncryptionButton.setBackground(K.Color.MAIN_BUTTON_BACKGROUND)
    infoEditorPane.isEditable = false
    stdErrTextPane.isEditable = false
    infoEditorPane.isEnabled = false

    requestIdTextField.text = "-1"

    toolCombo.selectedItem = toolSource

    scriptPath = if (isRequest) {
      montoyaApi.persistence().extensionData().getString(K.KEYS.REQUEST_SCRIPT_PATH)
    } else {
      montoyaApi.persistence().extensionData().getString(K.KEYS.RESPONSE_SCRIPT_PATH)
    }

    val command = getCommandFromPath(montoyaApi.persistence(), scriptPath)
    infoEditorPane.text = "$command $scriptPath"
  }

  private fun layout() {
    val top = JPanel(MigLayout("insets 0"))
    val bottom = JPanel(MigLayout("insets 0"))

    val left = JPanel(MigLayout("insets 0"))
    val right = JPanel(MigLayout("insets 0"))

    left.add(JLabel("Edited"), "alignx center, wrap")
    if (isRequest) {
      top.add(requestEditor.uiComponent(), "grow, push")
      left.add(requestTransformed.uiComponent(), "grow, push")
    } else {
      top.add(responseEditor.uiComponent(), "grow, push")
      left.add(responseTransformed.uiComponent(), "grow, push")
    }

    top.add(requestEditor.uiComponent(), "grow, push")

    val horizontal = JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom)
    val vertical = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right)

    horizontal.resizeWeight = 0.5

    bottom.add(vertical, "grow, push")

    right.add(separator("Output", "center", false), "alignx center, wrap")
    right.add(stdErrScroll, "grow, push")

    mainPanel.add(horizontal, "grow, wrap, push")

    val buttonsPanel = JPanel(MigLayout("insets 0"))

    buttonsPanel.add(testDecryptionButton)
    buttonsPanel.add(JPanel(), "growx, pushx")
    buttonsPanel.add(JLabel("Tool:"))
    buttonsPanel.add(toolCombo)
    buttonsPanel.add(JLabel("MessageId:"))
    buttonsPanel.add(requestIdTextField)
    buttonsPanel.add(JPanel(), "growx, pushx")
    buttonsPanel.add(testEncryptionButton)

    mainPanel.add(buttonsPanel, "growx, pushx")
  }

  private fun execute(action: String) {
    if (requestResponse == null) return
    var source: String
    var prepared: HashMap<String?, String?>

    val messageId = requestIdTextField.text.toIntOrNull() ?: -1
    val selectedSource = toolCombo.selectedItem as String

    if (isRequest) {
      prepared = Utils.prepareRequestForExecutor(
        requestEditor.request,
        messageId,
        selectedSource
      )
      source = "request"
    } else {
      val url = KUtils.Url.clean(requestResponse!!.request().url())
      prepared = Utils.prepareResponseForExecutor(
        responseEditor.response,
        url,
        messageId,
        selectedSource
      )
      source = "response"
    }

    val executed = Executor.execute(montoyaApi, action, source, prepared)

    if (isRequest) {
      requestTransformed.request = Utils.executorToHttpRequest(
        requestEditor.request, executed)
    } else {
      responseTransformed.response = Utils.executorToHttpResponse(
        responseEditor.response, executed)
    }

    val sb = StringBuilder()

    if (!Utils.checkScriptVersion(executed.version) && executed.error.isBlank()) {
      sb.append("<div style='color:red'>${K.Error.SCRIPT_NOT_SUPORTED}</div>\n")
    }
    sb.append(KUtils.escapeHtml(executed.stdErr)).append(System.lineSeparator())
    sb.append("<div style='color:red;background:black'>${KUtils.escapeHtml(executed.error)}</div>")

    stdErrTextPane.text = sb.toString()
  }

  fun setRequestResponse(requestResponse: HttpRequestResponse?) {
    this.requestResponse = requestResponse
  }

  fun setRequest(request: HttpRequest?) {
    requestEditor.request = request
    updateUi()
  }

  fun setResponse(response: HttpResponse?) {
    responseEditor.response = response
    updateUi()
  }

  private fun updateUi() {
    requestTransformed.request = HttpRequest.httpRequest()
    responseTransformed.response = HttpResponse.httpResponse()
    stdErrTextPane.text = ""
  }

}