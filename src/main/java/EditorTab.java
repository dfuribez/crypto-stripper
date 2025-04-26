import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
  private JTextArea stdErrTextArea;
  private JSplitPane outputSplitPane;
  private JLabel messageLabel;
  private JSplitPane contentSplitpane;

  MontoyaApi api;

  boolean isRequest;

  HttpRequestResponse requestResponse;

  HttpResponseEditor responseEditor;
  HttpRequestEditor requestEditor;

  HttpRequestEditor requestTransformed;
  HttpResponseEditor responseTransformed;

  EditorTab(MontoyaApi api, boolean isRequest) {
    this.isRequest = isRequest;
    this.api = api;
    stdErrPanel.setBorder(new TitledBorder("stderr:"));

    requestEditor = api.userInterface().createHttpRequestEditor();
    responseEditor = api.userInterface().createHttpResponseEditor();

    requestTransformed = api.userInterface().createHttpRequestEditor();
    responseTransformed = api.userInterface().createHttpResponseEditor();

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

    testEncryptionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        execute("encrypt");
      }
    });
    testDecryptionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        execute("decrypt");
      }
    });
  }

  public void setRequestResponse(
      HttpRequestResponse requestResponse
  ) {
    this.requestResponse = requestResponse;
  }

  private void execute(String action) {
    HashMap<String, String> prepared = new HashMap<>();
    String source;

    updateUi();
    if (isRequest) {
      prepared = Utils.prepareRequestForExecutor(
              requestResponse.request(), 0);
      source = "request";
    } else {
      String url =
          Utils.removeQueryFromUrl(requestResponse.request().url());
      prepared = Utils.prepareResponseForExecutor(
          requestResponse.response(), url, 0);
      source = "response";
    }

    ExecutorResponse executed = Executor.execute(api, action, source, prepared);

    if (isRequest) {
      requestTransformed.setRequest(
          Utils.executorToHttpRequest(requestResponse.request(), executed));
    } else {
      responseTransformed.setResponse(
          Utils.executorToHttpResponse(requestResponse.response(), executed));
    }

    stdErrTextArea.setText(executed.getStdErr());
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
    if (isRequest) {
      messageLabel.setText(api.persistence().extensionData().getString(
          Constants.REQUEST_SCRIPT_PATH_KEY));
    } else {
      messageLabel.setText(api.persistence().extensionData().getString(
          Constants.RESPONSE_CHECKBOX_STATUS_KEY));

    }
  }
}
