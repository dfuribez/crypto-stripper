import javax.swing.*;
import javax.swing.border.TitledBorder;

public class EditorTab {
  public JPanel panel1;
  private JTextArea commandTextArea;
  private JButton testButton;
  private JTextArea textArea2;
  private JTextArea textArea3;
  private JPanel commandPanel;
  private JPanel stdOutPanel;
  private JPanel stdErrPanel;

  EditorTab() {
    this.commandPanel.setBorder(new TitledBorder("Command:"));
    this.stdErrPanel.setBorder(new TitledBorder("stderr:"));
    this.stdOutPanel.setBorder(new TitledBorder("stdout:"));
  }


  public void setCommand(String command) {
    this.commandTextArea.setText(command);
  }
}
