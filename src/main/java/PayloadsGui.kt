import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.params.ParsedHttpParameter
import net.miginfocom.swing.MigLayout
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import java.util.function.Consumer
import javax.swing.*
import javax.swing.JFileChooser

class PayloadsGui(
  montoyaApi: MontoyaApi
) : JDialog(
  montoyaApi.userInterface().swingUtils().suiteFrame(),
  "Insert Payload",
  true
  ) {
  private val mainPanel = JPanel(MigLayout("fill"))

  private val buttonOK = JButton("Insert")
  private val buttonCancel = JButton("Cancel")
  private val insertRandomButton = JButton("Random")
  private val selectFileButton = JButton("Select")

  private val lenghtLabel = JLabel("0")

  private val textRepeat = JTextField()
  private val textLenght = JTextField()
  private val fileTextField = JTextField()

  private val previewTextArea = JTextArea(5, 20)

  var base64RadioButton: JRadioButton = JRadioButton("Base 64")
  var urlEncodeRadioButton: JRadioButton = JRadioButton("URL encondign")
  private val plainTextRadioButton = JRadioButton("Plain text")

  private val repeatTimesRadio = JRadioButton("Times")
  private val repeatBytesRadio = JRadioButton("Bytes")

  private val parametersCombo = JComboBox<String?>()

  private val scrollPreview = JScrollPane(previewTextArea)

  var selectedText: ByteArray? = null

  var fileChooser: JFileChooser = JFileChooser()
  var burpMainFrame: Frame? = null

  val URL_SAFE_CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
  val RANDOM: Random = Random()

  var selectedFile: String? = null

  init {
    setLocationRelativeTo(montoyaApi.userInterface().swingUtils().suiteFrame())
    contentPane = mainPanel
    rootPane.defaultButton = buttonOK

    addWindowListener( object : WindowAdapter() {
      override fun windowClosing(e: WindowEvent?) {
        onCancel()
      }
    })

    initListeners()
    initialize()
    initLayout()
  }

  private fun initListeners() {
    buttonOK.addActionListener { onOK() }
    buttonCancel.addActionListener { onCancel() }

    selectFileButton.addActionListener { openFile() }
  }

  private fun initialize() {
    buttonOK.setBackground(K.Color.MAIN_BUTTON_BACKGROUND)

    textLenght.text = "100"
    fileTextField.isEditable = false

    lenghtLabel.horizontalAlignment = SwingConstants.CENTER

    previewTextArea.isEditable = false
    previewTextArea.lineWrap = true

    val encondings = ButtonGroup()
    encondings.add(base64RadioButton)
    encondings.add(urlEncodeRadioButton)
    encondings.add(plainTextRadioButton)

    plainTextRadioButton.isSelected = true

    val repeat = ButtonGroup()
    repeat.add(repeatBytesRadio)
    repeat.add(repeatTimesRadio)

    repeatBytesRadio.isSelected = true
  }

  private fun initLayout() {
    // Separator
    val separator = utils.separator("Characters", "center", true, null)
    mainPanel.add(separator, "span, growx, pushx, wrap")

    // Characters
    val characters = JPanel(MigLayout())

    characters.add(JLabel("Strings:"))
    characters.add(textRepeat, "growx, pushx, span 3")
    characters.add(lenghtLabel, "wrap, alignx center")

    characters.add(JLabel("Repeat:"))
    characters.add(textLenght, "growx, pushx")
    characters.add(repeatBytesRadio)
    characters.add(repeatTimesRadio)
    characters.add(insertRandomButton, "wrap")

    characters.add(JLabel("Preview:"))
    characters.add(scrollPreview, "span, grow, push")

    mainPanel.add(characters, "span, growx, pushx, wrap")

    // --------------
    val separator2 = utils.separator("Files", "center", true, null)
    mainPanel.add(separator2, "span, growx, pushx, wrap")

    // Files
    val files = JPanel(MigLayout())

    files.add(JLabel("File:"))
    files.add(fileTextField, "growx, pushx")
    files.add(selectFileButton, "sg btn")

    mainPanel.add(files, "span, growx, pushx, wrap")

    // --------------
    val separator3 = utils.separator("Output", "center", true, null)
    mainPanel.add(separator3, "span, growx, pushx, wrap")

    // Options
    val options = JPanel(MigLayout())

    options.add(JLabel("Encoding:"))
    options.add(base64RadioButton)
    options.add(urlEncodeRadioButton)
    options.add(plainTextRadioButton, "wrap")

    options.add(JLabel("Insertion point:"))
    options.add(parametersCombo, "growx, pushx, span, wrap, wmax 400")

    mainPanel.add(options, "span, pushx, growx, wrap")

    mainPanel.add(JPanel(), "growx")
    mainPanel.add(buttonCancel)
    mainPanel.add(buttonOK)
  }

  fun clear() {
    textRepeat.text = ""
    fileTextField.text = ""
    previewTextArea.text = ""
    selectedFile = null
  }

  fun setParameters(parameters: List<ParsedHttpParameter?>) {
    clear()
    parametersCombo.removeAllItems()
    parametersCombo.addItem("REQUEST - SELECTION POINT")
    parameters.forEach(Consumer { p: ParsedHttpParameter? ->
      parametersCombo.addItem(p!!.type().name + " - " + p.name())
    })
  }

  private fun openFile() {
    val response = fileChooser.showOpenDialog(burpMainFrame)
    if (response != JFileChooser.APPROVE_OPTION) return

    selectedFile = fileChooser.selectedFile.absolutePath
    fileTextField.text = selectedFile
    textRepeat.text = ""
  }

  private fun onCancel() {
    dispose()
  }

  fun getSelectedParameter(): String { return parametersCombo.selectedItem as String }
  private fun onOK() { dispose() }
}
