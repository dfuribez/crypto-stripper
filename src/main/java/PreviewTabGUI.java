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
import javax.swing.border.TitledBorder;
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
  private JPanel stdErrPanel = new JPanel();

  private JTextPane stdErrTextArea = new JTextPane();

  private JEditorPane infoEditorPane = new JEditorPane();

  private JTextField requestIdTextField = new JTextField(4);

  private JComboBox<String> toolCombo = new JComboBox<>(Constants.TOOLS);

  MontoyaApi api;

  boolean isRequest;

  HttpRequestResponse requestResponse;

  HttpResponseEditor responseEditor;
  HttpRequestEditor requestEditor;

  HttpRequestEditor requestTransformed;
  HttpResponseEditor responseTransformed;

  String toolSource;
  StyledDocument doc;

  Style warningStyle;
  Style defaultStyle;
  Style errorStyle;

  String scriptPath;


  PreviewTabGUI(MontoyaApi montoyaApi, boolean isRequest, String toolSource) {
    this.isRequest = isRequest;
    this.api = montoyaApi;

    this.toolSource = toolSource;


    initialize();


    stdErrPanel.setBorder(new TitledBorder("stderr:"));

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
    infoEditorPane.setEditable(false);
    stdErrTextArea.setEditable(false);
    infoEditorPane.setEnabled(false);

    requestIdTextField.setText("-1");

    toolCombo.setSelectedItem(toolSource);

    if (isRequest) {
      scriptPath = api.persistence().extensionData().getString(Constants.REQUEST_SCRIPT_PATH_KEY);
    } else {
      scriptPath = api.persistence().extensionData().getString(Constants.RESPONSE_SCRIPT_PATH_KEY);
    }

    String command = Utils.getCommandFromPath(api.persistence(), scriptPath);
    infoEditorPane.setText(String.format(Constants.PREVIEW_INFO_TEMPLATE, command, scriptPath));
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

    right.add(new JLabel("Output"), "alignx center, wrap");
    right.add(stdErrTextArea, "grow, push");


    mainPanel.add(infoEditorPane, "grow, push, wrap");

    JPanel options = new JPanel(new MigLayout());

    options.add(new JLabel("Tool:"));
    options.add(toolCombo);
    options.add(new JLabel("MessageId:"));
    options.add(requestIdTextField);
    mainPanel.add(options, "alignx center, wrap");

    mainPanel.add(horizontal, "grow, push, wrap");

    JPanel buttonsPanel = new JPanel(new MigLayout());


    buttonsPanel.add(testDecryptionButton);

    buttonsPanel.add(new JPanel(), "growx, pushx");

    buttonsPanel.add(openScriptButton);
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
        Utils.removeQueryFromUrl(requestResponse.request().url());
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
      doc.insertString(doc.getLength(), Constants.SCRIPT_NOT_SUPORTED, warningStyle);
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
