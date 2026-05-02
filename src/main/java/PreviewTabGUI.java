import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import models.ExecutorOutput;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.HashMap;

public class PreviewTabGUI {
  private JButton testDecryptionButton = new JButton("Decrypt");
  private JButton testEncryptionButton = new JButton("Encrypt");
  private JButton openScriptButton = new JButton("Open");

  public JPanel mainPanel = new JPanel(new MigLayout("insets 0"));

  private JTextPane stdErrTextArea = new JTextPane();
  private JTextField requestIdTextField = new JTextField(4);

  private JEditorPane infoEditorPane = new JEditorPane();

  private JComboBox<String> toolCombo = new JComboBox<>(K.Gen.TOOLS);

  private JScrollPane stdErrScroll = new JScrollPane(stdErrTextArea);

  MontoyaApi api;

  boolean isRequest;

  HttpRequestResponse requestResponse;

  HttpResponseEditor responseEditor;
  HttpResponseEditor responseTransformed;

  HttpRequestEditor requestEditor;
  HttpRequestEditor requestTransformed;

  String toolSource;
  String scriptPath;

  StyledDocument doc;

  Style warningStyle;
  Style defaultStyle;
  Style errorStyle;


  PreviewTabGUI(MontoyaApi montoyaApi, boolean isRequest, String toolSource) {
    this.isRequest = isRequest;
    this.api = montoyaApi;

    this.toolSource = toolSource;

    initialize();

    requestEditor = api.userInterface().createHttpRequestEditor();
    responseEditor = api.userInterface().createHttpResponseEditor();

    requestTransformed = api.userInterface().createHttpRequestEditor(
      EditorOptions.READ_ONLY);
    responseTransformed = api.userInterface().createHttpResponseEditor(
      EditorOptions.READ_ONLY);

    int fontSize = UIManager.getFont("TextPane.font").getSize();

    doc = stdErrTextArea.getStyledDocument();
    warningStyle = doc.addStyle("Orange", null);
    StyleConstants.setForeground(warningStyle, Color.ORANGE);
    StyleConstants.setBold(warningStyle, true);
    StyleConstants.setBackground(warningStyle, Color.BLACK);
    StyleConstants.setFontSize(warningStyle, fontSize + 2);

    defaultStyle = doc.addStyle("Default", null);
    StyleConstants.setBold(defaultStyle, false);

    errorStyle = doc.addStyle("Red", null);
    StyleConstants.setForeground(errorStyle, Color.RED);
    StyleConstants.setFontSize(errorStyle, fontSize + 2);
    StyleConstants.setBackground(errorStyle, Color.BLACK);

    testEncryptionButton.addActionListener(actionEvent -> execute("encrypt"));
    testDecryptionButton.addActionListener(actionEvent -> execute("decrypt"));
    openScriptButton.addActionListener(actionEvent -> Utils.openFolder(scriptPath));

    setLayout();
}

  private void initialize() {
    testDecryptionButton.setBackground(K.Color.MAIN_BUTTON_BACKGROUND);
    testEncryptionButton.setBackground(K.Color.MAIN_BUTTON_BACKGROUND);
    infoEditorPane.setEditable(false);
    stdErrTextArea.setEditable(false);
    infoEditorPane.setEnabled(false);

    requestIdTextField.setText("-1");

    toolCombo.setSelectedItem(toolSource);

    if (isRequest) {
      scriptPath = api.persistence().extensionData().getString(K.KEYS.REQUEST_SCRIPT_PATH);
    } else {
      scriptPath = api.persistence().extensionData().getString(K.KEYS.RESPONSE_SCRIPT_PATH);
    }

    String command = KUtils.getCommandFromPath(api.persistence(), scriptPath);
    infoEditorPane.setText(String.format(K.Gen.PREVIEW_INFO_TEMPLATE, command, scriptPath));
  }

  private void setLayout() {
    JPanel top = new JPanel(new MigLayout("insets 0"));
    JPanel bottom = new JPanel(new MigLayout("insets 0"));

    JPanel left = new JPanel(new MigLayout("insets 0"));
    JPanel right = new JPanel(new MigLayout("insets 0"));

    left.add(new JLabel("Edited"), "alignx center, wrap");
    if (isRequest) {
      top.add(requestEditor.uiComponent(), "grow, push");
      left.add(requestTransformed.uiComponent(), "grow, push");
    } else {
      top.add(responseEditor.uiComponent(), "grow, push");
      left.add(responseTransformed.uiComponent(), "grow, push");
    }

    top.add(requestEditor.uiComponent(), "grow, push");

    JSplitPane horizontal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
    JSplitPane vertical = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);

    horizontal.setResizeWeight(0.5);

    bottom.add(vertical, "grow, push");

    right.add(KUtils.separator("Output", "center", false), "alignx center, wrap");
    right.add(stdErrScroll, "grow, push");

    mainPanel.add(horizontal, "grow, wrap, push");

    JPanel buttonsPanel = new JPanel(new MigLayout("insets 0"));

    buttonsPanel.add(testDecryptionButton);
    buttonsPanel.add(new JPanel(), "growx, pushx");
    buttonsPanel.add(new JLabel("Tool:"));
    buttonsPanel.add(toolCombo);
    buttonsPanel.add(new JLabel("MessageId:"));
    buttonsPanel.add(requestIdTextField);
    buttonsPanel.add(new JPanel(), "growx, pushx");
    buttonsPanel.add(testEncryptionButton);

    mainPanel.add(buttonsPanel, "growx, pushx");
  }

public void setRequestResponse(HttpRequestResponse requestResponse) {
  this.requestResponse = requestResponse;
}

private void execute(String action) {
  HashMap<String, String> prepared;
  String source;

  updateUi();

  int messageId = Utils.stringToInt(requestIdTextField.getText());
  String selectedToolSource = (String) toolCombo.getSelectedItem();

  if (isRequest) {
    prepared = Utils.prepareRequestForExecutor(
        requestEditor.getRequest(), messageId, selectedToolSource);
    source = "request";
  } else {
    String url =
        KUtils.Url.clean(requestResponse.request().url());
    prepared = Utils.prepareResponseForExecutor(
        responseEditor.getResponse(), url, messageId, selectedToolSource);
    source = "response";
  }

  ExecutorOutput executed = Executor.execute(api, action, source, prepared);

  if (isRequest) {
    requestTransformed.setRequest(
        Utils.executorToHttpRequest(requestEditor.getRequest(), executed));
  } else {
    responseTransformed.setResponse(
        Utils.executorToHttpResponse(responseEditor.getResponse(), executed));
  }

  try {
    if (!Utils.checkScriptVersion(executed.version) && executed.error.isBlank()){
      doc.insertString(doc.getLength(), K.Error.SCRIPT_NOT_SUPORTED.stripIndent(), warningStyle);
      doc.insertString(doc.getLength(), "\n\n", defaultStyle);
    }
    doc.insertString(doc.getLength(), executed.stdErr, defaultStyle);
    doc.insertString(doc.getLength(), "\n\n", defaultStyle);
    doc.insertString(doc.getLength(), executed.error, errorStyle);
  } catch (BadLocationException e) {
    stdErrTextArea.setText(executed.stdErr + "\n" + executed.error);
  }
}

  public void setContent(HttpRequest request) {
    requestEditor.setRequest(request);
    updateUi();
  }

  public void setContent(HttpResponse response) {
    responseEditor.setResponse(response);
    updateUi();
  }

  private void updateUi() {
    requestTransformed.setRequest(HttpRequest.httpRequest());
    responseTransformed.setResponse(HttpResponse.httpResponse());
    stdErrTextArea.setText("");
  }
}
