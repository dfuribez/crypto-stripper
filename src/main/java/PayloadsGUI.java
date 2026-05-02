import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class PayloadsGUI extends JDialog {
  private JPanel mainPanel = new JPanel(new MigLayout("fill"));

  private JButton buttonOK = new JButton("Insert");
  private JButton buttonCancel = new JButton("Cancel");
  private JButton insertRandomButton = new JButton("Random");
  private JButton selectFileButton = new JButton("Select");

  private JLabel lenghtLabel = new JLabel("0");

  private JTextField textRepeat = new JTextField();
  private JTextField textLenght = new JTextField();
  private JTextField fileTextField = new JTextField();

  private JTextArea previewTextArea = new JTextArea(5, 20);

  public JRadioButton base64RadioButton = new JRadioButton("Base 64");
  public JRadioButton URLEncodeRadioButton = new JRadioButton("URL encondign");
  private JRadioButton plainTextRadioButton = new JRadioButton("Plain text");

  private JRadioButton repeatTimesRadio = new JRadioButton("Times");
  private JRadioButton repeatBytesRadio = new JRadioButton("Bytes");

  private JComboBox<String> parametersCombo = new JComboBox<>();

  private JScrollPane scrollPreview = new JScrollPane(previewTextArea);

  static byte[] selectedText;
  MontoyaApi montoyaApi;

  JFileChooser fileChooser = new JFileChooser();
  Frame burpMainFrame;

  private static final String URL_SAFE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
  private static final Random RANDOM = new Random();


  public PayloadsGUI(MontoyaApi montoyaApi) {
    super(montoyaApi.userInterface().swingUtils().suiteFrame(), "Insert payload", true);

    this.montoyaApi = montoyaApi;
    this.burpMainFrame = montoyaApi.userInterface().swingUtils().suiteFrame();

    setLocationRelativeTo(this.burpMainFrame);
    setContentPane(mainPanel);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(e -> onOK());

    buttonCancel.addActionListener(e -> onCancel());

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    mainPanel.registerKeyboardAction(e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
    );

    textRepeat.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { handle(); }
      public void removeUpdate(DocumentEvent e) { handle(); }
      public void changedUpdate(DocumentEvent e) {}

      private void handle() {
        String text = textRepeat.getText();
        lenghtLabel.setText(String.valueOf(text.length()));
        generateString();
      }
    });

    textLenght.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { handle(); }
      public void removeUpdate(DocumentEvent e) { handle(); }
      public void changedUpdate(DocumentEvent e) {}

      private void handle() {
        generateString();
      }
    });

    selectFileButton.addActionListener(actionEvent -> {
      int response = fileChooser.showOpenDialog(burpMainFrame);

      if (response == JFileChooser.APPROVE_OPTION) {
        String path = fileChooser.getSelectedFile().getAbsolutePath();

        try {
          PayloadsGUI.selectedText = Files.readAllBytes(Paths.get(path));
          fileTextField.setText(path);
          textRepeat.setText("");
        } catch (Exception e) {
          montoyaApi.logging().logToError("Could not open file: " + path);
          montoyaApi.logging().logToError(e.toString());
        }
      }
    });
    insertRandomButton.addActionListener(actionEvent -> {
      int length;

      try {
        length = Integer.parseInt(textLenght.getText());
      } catch (NumberFormatException e) {
        length = 0;
      }

      if (length > 0) {
        String random = generate(length);
        previewTextArea.setText(random);
        selectedText = random.getBytes(StandardCharsets.UTF_8);
      }
    });

    initialize();
    setLayout();
  }

  private void initialize() {
    buttonOK.setBackground(K.Color.MAIN_BUTTON_BACKGROUND);

    textLenght.setText("100");
    fileTextField.setEditable(false);

    lenghtLabel.setHorizontalAlignment(SwingConstants.CENTER);

    previewTextArea.setEditable(false);
    previewTextArea.setLineWrap(true);

    ButtonGroup encondings = new ButtonGroup();
    encondings.add(base64RadioButton);
    encondings.add(URLEncodeRadioButton);
    encondings.add(plainTextRadioButton);

    plainTextRadioButton.setSelected(true);

    ButtonGroup repeat = new ButtonGroup();
    repeat.add(repeatBytesRadio);
    repeat.add(repeatTimesRadio);

    repeatBytesRadio.setSelected(true);

  }

  private void setLayout() {

    // Separator
    JPanel separator = Utils.separator("Characters", "center", true);
    mainPanel.add(separator, "span, growx, pushx, wrap");

    // Characters
    JPanel characters = new JPanel(new MigLayout());

    characters.add(new JLabel("Strings:"));
    characters.add(textRepeat, "growx, pushx, span 3");
    characters.add(lenghtLabel, "wrap, alignx center");

    characters.add(new JLabel("Repeat:"));
    characters.add(textLenght, "growx, pushx");
    characters.add(repeatBytesRadio);
    characters.add(repeatTimesRadio);
    characters.add(insertRandomButton, "wrap");

    characters.add(new JLabel("Preview:"));
    characters.add(scrollPreview, "span, grow, push");

    mainPanel.add(characters, "span, growx, pushx, wrap");

    // --------------
    JPanel separator2 = Utils.separator("Files", "center", true);
    mainPanel.add(separator2, "span, growx, pushx, wrap");


    // Files
    JPanel files = new JPanel(new MigLayout());

    files.add(new JLabel("File:"));
    files.add(fileTextField, "growx, pushx");
    files.add(selectFileButton, "sg btn");


    mainPanel.add(files, "span, growx, pushx, wrap");

    // --------------
    JPanel separator3 = Utils.separator("Output", "center", true);
    mainPanel.add(separator3, "span, growx, pushx, wrap");

    // Options
    JPanel options = new JPanel(new MigLayout());

    options.add(new JLabel("Encoding:"));
    options.add(base64RadioButton);
    options.add(URLEncodeRadioButton);
    options.add(plainTextRadioButton, "wrap");

    options.add(new JLabel("Insertion point:"));
    options.add(parametersCombo, "growx, pushx, span, wrap, wmax 400");

    mainPanel.add(options, "span, pushx, growx, wrap");

    mainPanel.add(new JPanel(), "growx");
    mainPanel.add(buttonCancel);
    mainPanel.add(buttonOK);

  }

  public void clear() {
    textRepeat.setText("");
    fileTextField.setText("");
    previewTextArea.setText("");
  }


  private void onOK() {
    dispose();
  }

  private void generateString() {
    int length;
    String toRepeat = textRepeat.getText();

    String tl = textLenght.getText();


    if (tl.length() <= 0 || toRepeat.length() <= 0) {
      previewTextArea.setText("");
      return;
    }

    try {
      length = Integer.parseInt(tl);
    } catch (NumberFormatException e) {
      length = 0;
    }

    if (length <= 0) {
      previewTextArea.setText("");
      return;
    }

    StringBuilder builder = new StringBuilder(length);

    while (builder.length() + toRepeat.length() <= length) {
      builder.append(toRepeat);
    }

    int remaining = length - builder.length();
    if (remaining > 0) {
      builder.append(toRepeat, 0, remaining);
    }

    previewTextArea.setText(builder.toString());
    selectedText = builder.toString().getBytes(StandardCharsets.UTF_8);

  }


  private static String generate(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int index = RANDOM.nextInt(URL_SAFE_CHARS.length());
      sb.append(URL_SAFE_CHARS.charAt(index));
    }
    return sb.toString();
  }

  public void setParameters(List<ParsedHttpParameter> parameters) {
    clear();
    parametersCombo.removeAllItems();
    parametersCombo.addItem("REQUEST - SELECTION POINT");
    parameters.forEach(p -> {
      parametersCombo.addItem(p.type().name() + " - " + p.name());
    });
  }

  public String getSelectedParameter() {
    return (String) parametersCombo.getSelectedItem();
  }

  private void onCancel() {
    selectedText = null;
    dispose();
  }
}
