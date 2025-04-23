import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.RawEditor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class EditorTab {
  public JPanel panel1;
  private JTextArea commandTextArea;
  private JButton testDecryptionButton;
  private JTextArea stdOutText;
  private JPanel commandPanel;
  private JPanel stdOutPanel;
  private JPanel stdErrPanel;
  private JButton testEncryptionButton;
  private JPanel buttonsPanel;
  private JTextArea stdOutTextArea;
  private JTextArea stdErrTextArea;

  MontoyaApi api;
  RawEditor contentEditor;

  boolean isRequest;

  HttpRequestResponse requestResponse;


  EditorTab(
      MontoyaApi api,
      boolean isRequest
  ) {
    this.isRequest = isRequest;
    this.api = api;
    commandPanel.setBorder(new TitledBorder("Command:"));
    stdErrPanel.setBorder(new TitledBorder("stderr:"));
    stdOutPanel.setBorder(new TitledBorder("stdout:"));

    contentEditor = api.userInterface().createRawEditor();
    contentEditor.setEditable(false);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    commandPanel.add(contentEditor.uiComponent(), gbc);

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
    String stdOutBox = "";

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

    ExecutorResponse executed = Executor.execute(
        api,
        action,
        source,
        prepared
    );


    if (isRequest) {
      HttpRequest request =
          Utils.executorToHttp(requestResponse.request(), executed);

      stdOutBox = executed.getError();

      if (stdOutBox.isBlank()) {
        stdOutBox = request.toString();
      }
    } else {
      HttpResponse response =
          Utils.executorToHttpResponse(requestResponse.response(), executed);
      stdOutBox = executed.getError();

      if (stdOutBox.isBlank()) {
        stdOutBox = response.toString();
      }
    }

    stdOutTextArea.setText(stdOutBox);
    stdErrTextArea.setText(executed.getStdErr());
  }

  public void setCommand(ByteArray content) {
    contentEditor.setContents(content);
  }
}
