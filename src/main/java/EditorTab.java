import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class EditorTab {
  public JPanel panel1;
  private JButton testDecryptionButton;
  private JTextArea stdOutText;
  private JPanel commandPanel;
  private JPanel stdOutPanel;
  private JPanel stdErrPanel;
  private JButton testEncryptionButton;
  private JPanel buttonsPanel;
  private JTextArea stdOutTextArea;
  private JTextPane stdErrTextArea;
  private JSplitPane outputSplitPane;
  private JSplitPane contentSplitpane;
  private JButton openScriptButton;

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


  EditorTab(MontoyaApi api, boolean isRequest, String toolSource) {
    this.isRequest = isRequest;
    this.api = api;

    this.toolSource = toolSource;

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

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    if (isRequest) {
      commandPanel.add(requestEditor.uiComponent(), gbc);
      stdOutPanel.add(requestTransformed.uiComponent(), gbc);
    } else {
      commandPanel.add(responseEditor.uiComponent(), gbc);
      stdOutPanel.add(responseTransformed.uiComponent(), gbc);
    }
    outputSplitPane.setResizeWeight(0.5);

    testEncryptionButton.addActionListener(actionEvent -> execute("encrypt"));
    testDecryptionButton.addActionListener(actionEvent -> execute("decrypt"));
    openScriptButton.addActionListener(actionEvent -> {
      String path;
      if (isRequest) {
        path = api.persistence().extensionData().getString(Constants.REQUEST_SCRIPT_PATH_KEY);
      } else {
        path = api.persistence().extensionData().getString(Constants.RESPONSE_SCRIPT_PATH_KEY);
      }
      Utils.openFolder(path);
    });
  }

  public void setRequestResponse(HttpRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }

  private void execute(String action) {
    HashMap<String, String> prepared = new HashMap<>();
    String source;

    updateUi();
    if (isRequest) {
      prepared = Utils.prepareRequestForExecutor(
          requestEditor.getRequest(), -1, toolSource);
      source = "request";
    } else {
      String url =
          Utils.removeQueryFromUrl(requestResponse.request().url());
      prepared = Utils.prepareResponseForExecutor(
          responseEditor.getResponse(), url, -1, toolSource);
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
      if (!Utils.checkScriptVersion(executed.getVersion())
      && executed.getError().isBlank()){
        doc.insertString(doc.getLength(), Constants.SCRIPT_NOT_SUPORTED, warningStyle);
        doc.insertString(doc.getLength(), "\n\n", defaultStyle);
      }
      doc.insertString(doc.getLength(), executed.getStdErr(), defaultStyle);
      doc.insertString(doc.getLength(), "\n\n", defaultStyle);
      doc.insertString(doc.getLength(), executed.getError(), errorStyle);
    } catch (BadLocationException e) {
      stdErrTextArea.setText(executed.getStdErr() + "\n" + executed.getError());
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
