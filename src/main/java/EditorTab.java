import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.editor.RawEditor;
import jdk.jshell.execution.Util;

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

  MontoyaApi api;
  RawEditor contentEditor;
  RawEditor stdErrEditor;
  RawEditor stdOutEditor;


  HttpRequestResponse requestResponse;


  EditorTab(MontoyaApi api) {
    this.api = api;
    this.commandPanel.setBorder(new TitledBorder("Command:"));
    this.stdErrPanel.setBorder(new TitledBorder("stderr:"));
    this.stdOutPanel.setBorder(new TitledBorder("stdout:"));

    this.contentEditor = api.userInterface().createRawEditor();
    this.stdErrEditor = api.userInterface().createRawEditor();
    this.stdOutEditor = api.userInterface().createRawEditor();

    this.stdOutEditor.setEditable(false);
    this.stdErrEditor.setEditable(false);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    this.commandPanel.add(this.contentEditor.uiComponent(), gbc);
    this.stdErrPanel.add(this.stdErrEditor.uiComponent(), gbc);
    this.stdOutPanel.add(this.stdOutEditor.uiComponent(), gbc);

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
    HashMap<String, String> prepared =
        Utils.prepareRequestForExecutor(
            this.requestResponse.request(), 0);

    ExecutorResponse executed = Executor.execute(
        this.api,
        action,
        "request",
        prepared
    );

    HttpRequest output =
        Utils.executorToHttp(this.requestResponse.request(), executed);

    String error = executed.getError();

    if (error.isBlank()) {
      error = output.toString();
    }

    this.stdOutEditor.setContents(ByteArray.byteArray(error));
    this.stdErrEditor.setContents(ByteArray.byteArray(
        executed.getStdErr().getBytes()));
  }

  public void setCommand(ByteArray content) {
    this.contentEditor.setContents(content);
  }
}
