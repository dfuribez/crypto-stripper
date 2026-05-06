import KUtils.separator
import burp.api.montoya.MontoyaApi
import burp.api.montoya.persistence.PersistedList
import net.miginfocom.swing.MigLayout
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.filechooser.FileNameExtensionFilter


class StripperTab(var montoyaApi: MontoyaApi) {
  private var panel1: JPanel = JPanel(MigLayout("fillx"))
  private val encryptorsPanel = JPanel(MigLayout())
   var requestCheckBox: JCheckBox = JCheckBox("Request:")
   var responseCheckBox: JCheckBox = JCheckBox("Response:")
   var forceInterceptInScopeCheckbox: JCheckBox = JCheckBox("Force Intercept In Scope")

  private val scopeList = JList<String>()
  private val blackList = JList<String>()
  private val forceInterceptList = JList<String>()

  private val restoreSettingsButton = JButton("Restore Settings")
  private val deleteSelectedScopeButton = JButton("Delete selection")
  private val deleteSelectedBlacklistButton = JButton("Delete selection")
  private val deleteSelectedForceButton = JButton("Delete selection")

  private val selectRequestScriptButton = JButton("Select request script")
  private val selectResponseScriptButton = JButton("Select response script")

  private val pathsPanel = JPanel(MigLayout())
  private val chooseNodeBinaryButton = JButton("Select path")
  private val choosePythonBinaryButton = JButton("Select path")
  private val setNodeDefaultButton = JButton("X")
  private val setPythonDefaultButton = JButton("X")


  private val nodePathTextField = JTextField()
  private val pythonPathTextField = JTextField()

  private val exportSettingsButton = JButton("Export Settings")
  private val importSettingsButton = JButton("Import Settings")

  private val selectNodeGlobalButton = JButton("Select path")
  private val selectPythonGlobalButton = JButton("Select path")

  private val globalBinariesPanel = JPanel(MigLayout())
  private val nodeGlobalTextEdit = JTextField("Global Node")
  private val pythonGlobalTextEdit = JTextField("Global Python")

  private val scopeListPanel = JPanel(MigLayout())
  private val blackListPanel = JPanel(MigLayout())
  private val forceListPanel = JPanel(MigLayout())

  private val jsTemplateButton = JButton("JS Template")
  private val pythonTemplateButton = JButton("Python Template")

  private val scopeUrlTextField = JTextField()
  private val addScopeUrlButton = JButton("Add")

  private val blackListUrlTextField = JTextField()
  private val addBlackListUrlButton = JButton("Add")

  private val forceUrlTextField = JTextField()
  private val addForceUrlButton = JButton("Add")

  private val versionTextArea = JTextArea(5, 20)

   var enableBlackListcheckbox: JCheckBox = JCheckBox("Enable")
   var enableForceListCheckbox: JCheckBox = JCheckBox("Enable")

   val requestScriptTextField = JTextField()
  private val responseScriptTextField = JTextField()

  private val openRequestButton = JButton("Open")
  private val openResponseButton = JButton("Open")

  private val scrollScope = JScrollPane(scopeList)
  private val scrollBlackList = JScrollPane(blackList)
  private val scrollForceInterceptList = JScrollPane(forceInterceptList)

  var fileChooser: JFileChooser = JFileChooser()

  init {
    versionTextArea.text = K.Gen.VERSION.trimIndent()
    loadSettings()
    addActions()
    initialize()
    layout()
  }

  private fun initialize() {
    nodePathTextField.isEditable = false
    nodePathTextField.border = null

    pythonPathTextField.isEditable = false
    pythonPathTextField.border = null

    requestScriptTextField.isEditable = false

    responseScriptTextField.isEditable = false

    versionTextArea.isEditable = false

    nodeGlobalTextEdit.isEditable = false
    nodeGlobalTextEdit.border = null

    pythonGlobalTextEdit.isEditable = false
    pythonGlobalTextEdit.border = null
  }

  private fun addActions() {
    selectRequestScriptButton.addActionListener {
      val filter = FileNameExtensionFilter("Python, JavaScript files", "py", "js")
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val path = Utils2.openChooser(parent, fileChooser, "Request Script", filter, true)
      if (path.isBlank()) return@addActionListener
      montoyaApi.persistence().extensionData().setString(K.KEYS.REQUEST_SCRIPT_PATH, path)
      requestScriptTextField.text = path
    }

    selectResponseScriptButton.addActionListener {
      val filter = FileNameExtensionFilter("Python, JavaScript files", "py", "js")
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val path = Utils2.openChooser(parent, fileChooser, "Response Script", filter, true)
      if (path.isBlank()) return@addActionListener
      montoyaApi.persistence().extensionData().setString(K.KEYS.RESPONSE_SCRIPT_PATH, path)
      responseScriptTextField.text = path
    }

    chooseNodeBinaryButton.addActionListener {
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val path = Utils2.openChooser(parent, fileChooser, "Node binary path", null, true)
      if (path.isEmpty()) return@addActionListener
      montoyaApi.persistence().extensionData().setString(K.KEYS.PROJECT_NODE_PATH, path)
      nodePathTextField.text = path
    }
    choosePythonBinaryButton.addActionListener {
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val path = Utils2.openChooser(parent, fileChooser, "Python binary path", null, true)
      if (path.isEmpty()) return@addActionListener
      montoyaApi.persistence().extensionData().setString(K.KEYS.PROJECT_PYTHON_PATH, path)
      pythonPathTextField.text = path
    }

    selectNodeGlobalButton.addActionListener {
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val path = Utils2.openChooser(parent, fileChooser, "Node binary path", null, true)
      if (path.isBlank()) return@addActionListener
      montoyaApi.persistence().preferences().setString(K.KEYS.GLOBAL_NODE_PATH, path)
      nodeGlobalTextEdit.text = path
    }

    selectPythonGlobalButton.addActionListener {
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val path = Utils2.openChooser(parent, fileChooser, "Python binary path", null, true)
      if (path.isBlank()) return@addActionListener
      montoyaApi.persistence().preferences().setString(K.KEYS.GLOBAL_PYTHON_PATH, path)
      pythonGlobalTextEdit.text = path
    }

    jsTemplateButton.addActionListener {
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val filter = FileNameExtensionFilter("JavaScript files", "js")
      val path = Utils2.openChooser(parent, fileChooser, "JS template", filter, false)
      if (path.isBlank()) return@addActionListener
      Utils.resourceToFile(montoyaApi, "template.js", path)
    }

    pythonTemplateButton.addActionListener {
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      val filter = FileNameExtensionFilter("Python files", "py")
      val path = Utils2.openChooser(parent, fileChooser, "Python template", filter, false)
      if (path.isBlank()) return@addActionListener
      Utils.resourceToFile(montoyaApi, "template.py", path)
    }

    setNodeDefaultButton.addActionListener {
      montoyaApi.persistence().extensionData().setString(K.KEYS.PROJECT_NODE_PATH, "")
      nodePathTextField.text = ""
    }
    setPythonDefaultButton.addActionListener {
      montoyaApi.persistence().extensionData().setString(K.KEYS.PROJECT_PYTHON_PATH, "")
      pythonPathTextField.text = ""
    }

    deleteSelectedScopeButton.addActionListener { updateScope("scope", "delete") }
    deleteSelectedBlacklistButton.addActionListener { updateScope("black", "delete") }
    deleteSelectedForceButton.addActionListener {  updateScope("force", "delete") }

    addScopeUrlButton.addActionListener{ updateScope("scope", "add") }
    addBlackListUrlButton.addActionListener{ updateScope("black", "add") }
    addForceUrlButton.addActionListener{ updateScope("force", "add") }

    restoreSettingsButton.addActionListener { clearSettings() }

    requestCheckBox.addActionListener { saveCurrentSettings() }
    responseCheckBox.addActionListener { saveCurrentSettings() }
    forceInterceptInScopeCheckbox.addActionListener { saveCurrentSettings() }
    enableBlackListcheckbox.addActionListener { saveCurrentSettings() }
    enableForceListCheckbox.addActionListener { saveCurrentSettings() }

    exportSettingsButton.addActionListener { exportSettings() }
    importSettingsButton.addActionListener { importSettings() }

    openRequestButton.addActionListener { openFolder(requestScriptTextField.text) }
    openResponseButton.addActionListener { openFolder(responseScriptTextField.text) }
  }

  private fun openFolder(path: String) {
    val result = Utils2.openFolder(path)
    val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
    if (!result) Utils2.showAlertMessage(parent, "Error opening containing folder")
  }

  private fun layout() {
    val emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5)

    pathsPanel.border = BorderFactory.createCompoundBorder(
      TitledBorder(BorderFactory.createEtchedBorder(), "Project paths:"),
      emptyBorder
    )

    globalBinariesPanel.border = BorderFactory.createCompoundBorder(
      TitledBorder(BorderFactory.createEtchedBorder(), "Global paths:"),
      emptyBorder
    )
    encryptorsPanel.add(requestCheckBox)
    encryptorsPanel.add(requestScriptTextField, "growx, pushx")
    encryptorsPanel.add(selectRequestScriptButton, "sg btn")
    encryptorsPanel.add(openRequestButton, "sg btn1, wrap")
    encryptorsPanel.add(responseCheckBox)
    encryptorsPanel.add(responseScriptTextField, "growx, pushx")
    encryptorsPanel.add(selectResponseScriptButton, "sg btn")
    encryptorsPanel.add(openResponseButton, "sg btn1, wrap")
    encryptorsPanel.add(forceInterceptInScopeCheckbox)

    panel1.add(encryptorsPanel, "growx, wrap")

    // --------
    val gpathsPanel = JPanel(MigLayout("", "[50%][50%]", ""))

    // Global binaries
    globalBinariesPanel.add(JLabel("Node:"))
    globalBinariesPanel.add(nodeGlobalTextEdit, "sg lbl, growx, pushx")
    globalBinariesPanel.add(selectNodeGlobalButton, "sg btn, align right, wrap")
    globalBinariesPanel.add(JLabel("Python:"))
    globalBinariesPanel.add(pythonGlobalTextEdit, "growx, pushx")
    globalBinariesPanel.add(selectPythonGlobalButton, "sg btn, align right, wrap")

    gpathsPanel.add(globalBinariesPanel, "sgy a, growx, pushx")

    // Project Paths
    pathsPanel.add(JLabel("Node:"))
    pathsPanel.add(nodePathTextField, "sg lbl, growx, pushx")
    pathsPanel.add(chooseNodeBinaryButton, "sg btn")
    pathsPanel.add(setNodeDefaultButton, "sg btn1, wrap")
    pathsPanel.add(JLabel("Python:"))
    pathsPanel.add(pythonPathTextField, "sg lbl, growx, pushx")
    pathsPanel.add(choosePythonBinaryButton, "sg btn")
    pathsPanel.add(setPythonDefaultButton, "sg btn1")

    gpathsPanel.add(pathsPanel, "sgy a, growx, pushx")

    panel1.add(gpathsPanel, "growx, wrap")

    // ---------------
    val separator = separator("Crypto Stripper scope", "center", true)

    panel1.add(separator, "growx, wrap")

    // ------
    val scopesPanel = JPanel(MigLayout("fill"))

    // Scope panel
    scopeListPanel.add(separator("Scope (?)", "center", false), "span, growx")
    scopeListPanel.add(scrollScope, "wrap, span 2, grow, push")
    scopeListPanel.add(deleteSelectedScopeButton, "alignx right, span 2, wrap")
    scopeListPanel.add(scopeUrlTextField, "growx, pushx")
    scopeListPanel.add(addScopeUrlButton)

    scopesPanel.add(scopeListPanel, "sg pn, grow")

    // BlackList panel
    blackListPanel.add(separator("Black List: (?)", "center", false), "span, growx")
    blackListPanel.add(scrollBlackList, "span 2, grow, push, wrap")
    blackListPanel.add(enableBlackListcheckbox, "alignx left")
    blackListPanel.add(deleteSelectedBlacklistButton, "alignx right, wrap")

    val addBlackListPanel = JPanel(MigLayout("insets 0"))

    addBlackListPanel.add(blackListUrlTextField, "growx, pushx")
    addBlackListPanel.add(addBlackListUrlButton)

    blackListPanel.add(addBlackListPanel, "span, growx")

    scopesPanel.add(blackListPanel, "sg pn, grow")

    // Force interept panel
    forceListPanel.add(separator("Force intercept: (?)", "center", false), "span, growx")

    forceListPanel.add(scrollForceInterceptList, "wrap, span 2, grow, push")
    forceListPanel.add(enableForceListCheckbox, "alignx left")
    forceListPanel.add(deleteSelectedForceButton, "alignx right, wrap")

    val addForcePanel = JPanel(MigLayout("insets 0"))

    addForcePanel.add(forceUrlTextField, "growx, pushx")
    addForcePanel.add(addForceUrlButton)

    forceListPanel.add(addForcePanel, "span, growx")

    scopesPanel.add(forceListPanel, "sg pn, grow")

    panel1.add(scopesPanel, "grow, wrap")

    // Buttons
    val buttonsPanel = JPanel(MigLayout())

    buttonsPanel.add(jsTemplateButton)
    buttonsPanel.add(pythonTemplateButton)
    buttonsPanel.add(JPanel(), "pushx, growx")
    buttonsPanel.add(restoreSettingsButton)
    buttonsPanel.add(JSeparator(SwingConstants.VERTICAL), "width 2!, growy")
    buttonsPanel.add(exportSettingsButton)
    buttonsPanel.add(importSettingsButton)

    panel1.add(JPanel(), "pushy, wrap")
    panel1.add(versionTextArea, "wrap")
    panel1.add(buttonsPanel, "growx")
  }


  private fun updateScope(source: String, action: String) {
    val target: JList<String>
    val selectedTextField: JTextField?
    val selectedScopeList: PersistedList<String>
    val key: String?
    val addUrl: String?

    val scope = Utils2.Settings.scope(montoyaApi)

    when (source) {
      "scope" -> {
        target = scopeList
        selectedScopeList = scope.scope
        key = K.KEYS.SCOPE_LIST
        selectedTextField = scopeUrlTextField
      }
      "black" -> {
        target = blackList
        selectedScopeList = scope.black
        key = K.KEYS.BLACK_LIST
        selectedTextField = blackListUrlTextField
      }
      "force" -> {
        target = forceInterceptList
        selectedScopeList = scope.force
        key = K.KEYS.FORCE_INTERCEPT_LIST
        selectedTextField = forceUrlTextField
      }
      else -> return
    }

    if (action == "delete") {
      val model = target.model as DefaultListModel<String>

      val selectedIndex = target.selectedIndex
      val selectedValue = target.selectedValue

      if (selectedIndex != -1 && selectedValue != null) {
        model.remove(selectedIndex)
        selectedScopeList.remove(selectedValue)
      }
    } else {
      addUrl = selectedTextField.text;
      val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
      if (!Utils2.isValidRegex(addUrl) || addUrl.isBlank()) {
        Utils2.showAlertMessage(parent, "Please check your url, it is not a valid regex");
        return;
      }

      if (Utils2.isUrlInScope(addUrl, selectedScopeList)) {
        Utils2.showAlertMessage(parent, "Url already in the scope");
        return;
      }

      selectedScopeList.add(addUrl);
      selectedTextField.text = "";
    }
    montoyaApi.persistence().extensionData().setStringList(key, selectedScopeList);
    loadSettings()
  }

  private fun clearSettings() {
    Utils2.Settings.clear(montoyaApi)
    loadSettings()
  }

  fun saveCurrentSettings() {
    val requestCheckboxStatus = requestCheckBox.isSelected
    val responseCheckboxStatus = responseCheckBox.isSelected
    val forceCheckboxStatus = forceInterceptInScopeCheckbox.isSelected

    val enableBlack = enableBlackListcheckbox.isSelected
    val enableForce = enableForceListCheckbox.isSelected

    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.FORCE_CHECKBOX_STATUS, forceCheckboxStatus
    )
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.REQUEST_CHECKBOX_STATUS, requestCheckboxStatus
    )
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.RESPONSE_CHECKBOX_STATUS, responseCheckboxStatus
    )
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.ENABLE_BLACK, enableBlack)
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.ENABLE_FORCE, enableForce)
  }

  fun loadSettings() {
    val settings = Utils2.Settings.load(montoyaApi)
    val scope = Utils2.Settings.scope(montoyaApi)

    requestCheckBox.isSelected = settings.requestEnabled
    responseCheckBox.isSelected = settings.responseEnabled
    forceInterceptInScopeCheckbox.isSelected = settings.forceInScope

    requestScriptTextField.text = settings.requestScriptPath
    responseScriptTextField.text = settings.responseScriptPath
    nodePathTextField.text = settings.projectNodePath
    pythonPathTextField.text = settings.projectPythonPath
    nodeGlobalTextEdit.text = settings.globalNodePath
    pythonGlobalTextEdit.text = settings.globalPythonPath

    enableForceListCheckbox.isSelected = settings.enableForce
    enableBlackListcheckbox.isSelected = settings.enableBlack

    val scopeModel = DefaultListModel<String>()
    scopeModel.addAll(scope.scope.map { it.toString() })
    scopeList.model = scopeModel

    val blackModel = DefaultListModel<String>()
    blackModel.addAll(scope.black.map { it.toString() })
    blackList.model = blackModel

    val forceModel = DefaultListModel<String>()
    forceModel.addAll(scope.force.map { it.toString() })
    forceInterceptList.model = forceModel
  }

  private fun importSettings() {
    val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
    val filter = FileNameExtensionFilter("JSON, configuration files", "json")
    val path = Utils2.openChooser(parent, fileChooser, "Import JSON settings",filter,true)

    if (path.isEmpty()) return
    Utils2.Settings.importFromJson(montoyaApi, path)
    loadSettings()
  }

  private fun exportSettings() {
    val parent = montoyaApi.userInterface().swingUtils().suiteFrame()
    val filter = FileNameExtensionFilter("JSON, configuration files", "json")
    val path = Utils2.openChooser(parent, fileChooser, "Save settings", filter, false)

    if (path.isEmpty()) return

    Utils2.Settings.exportToJson(montoyaApi, path)
  }

  fun getMainPanel() : JPanel { return panel1}
}
