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

  val textRepeat = JTextArea(5, 20)
  val textLenght = JTextField()
  private val fileTextField = JTextField()

  var base64RadioButton: JRadioButton = JRadioButton("Base 64")
  var urlEncodeRadioButton: JRadioButton = JRadioButton("URL encoding")
  private val plainTextRadioButton = JRadioButton("Plain text")

  private val repeatTimesRadio = JRadioButton("Times")
  val repeatBytesRadio = JRadioButton("Bytes")

  private val parametersCombo = JComboBox<String?>()


  private val scrollRepeat = JScrollPane(textRepeat)

  var selectedText: ByteArray? = null

  var fileChooser: JFileChooser = JFileChooser()
  var burpMainFrame: Frame? = null

  val URL_SAFE_CHARS = ('a'..'z') + ('A'..'Z') + ('0'..'9')
  val RANDOM: Random = Random()

  var selectedFile: String? = null
  var canceled: Boolean = false

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

    insertRandomButton.addActionListener {
      val lenght = textLenght.text.toIntOrNull() ?: 0
      val generated = (1..lenght)
        .map { URL_SAFE_CHARS.random() }
        .joinToString("")

      textRepeat.text = generated
    }

    selectFileButton.addActionListener { openFile() }
  }

  private fun initialize() {
    buttonOK.setBackground(K.Color.MAIN_BUTTON_BACKGROUND)

    textLenght.text = "100"
    fileTextField.isEditable = false

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
    mainPanel.add(separator, "growx, span, wrap")

    // Characters
    val strings = JPanel(MigLayout())

    mainPanel.add(JLabel("String:"), "aligny center")
    strings.add(scrollRepeat, "growx, pushx, wrap")
    strings.add(insertRandomButton, "aligny center, alignx right, sg btn, wrap")
    mainPanel.add(strings, "grow, wrap")


    val repeat = JPanel(MigLayout())
    mainPanel.add(JLabel("Repeat:"))
    repeat.add(textLenght, "growx, pushx")
    repeat.add(repeatBytesRadio)
    repeat.add(repeatTimesRadio)
    mainPanel.add(repeat, "growx, wrap")

    // --------------
    val separator2 = utils.separator("Files", "center", true, null)
    mainPanel.add(separator2, "span, growx, wrap")

    // Files
    val files = JPanel(MigLayout())
    mainPanel.add(JLabel("File:"), ", sg labels")
    files.add(fileTextField, "growx, pushx")
    files.add(selectFileButton, "sg btn")
    mainPanel.add(files, "grow, wrap")

    // --------------
    val separator3 = utils.separator("Output", "center", true, null)
    mainPanel.add(separator3, "span, growx, wrap")

    val options = JPanel(MigLayout())
    mainPanel.add(JLabel("Encoding:"))
    options.add(base64RadioButton)
    options.add(urlEncodeRadioButton)
    options.add(plainTextRadioButton, "wrap")
    mainPanel.add(options, "growx, alignx center, wrap")

    mainPanel.add(JLabel("Insertion point:"))
    mainPanel.add(parametersCombo, "growx, pushx, span, wrap, wmax 600")

    val buttons = JPanel(MigLayout())
    buttons.add(JLabel(""), "growx, pushx")
    buttons.add(buttonCancel, "sg btn")
    buttons.add(buttonOK, "sg btn")
    mainPanel.add(JLabel(""))
    mainPanel.add(buttons, "growx")
  }

  fun clear() {
    textRepeat.text = ""
    fileTextField.text = ""
    selectedFile = null
    canceled = false
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
    canceled = true
    dispose()
  }

  fun getSelectedParameter(): String { return parametersCombo.selectedItem as String }
  private fun onOK() { dispose() }
}
