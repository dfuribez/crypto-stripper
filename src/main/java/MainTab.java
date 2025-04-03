import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainTab {
  public JPanel panel1;
  private JPanel encryptorsPanel;
  public JCheckBox requestCheckBox;
  public JCheckBox responseCheckBox;
  public JCheckBox forceInterceptInScope;
  private JList scopeList;
  private JList blackList;
  private JList forceInterceptList;
  private JButton button1;
  private JButton deleteSelectedButton;
  private JButton deleteSelectedButton1;
  private JButton deleteSelectedButton2;
  private JButton RequestFileButton;
  private JButton selectResponseScriptButton;
  private JLabel requetsPathLabel;
  private JLabel responsePathLabel;


  MontoyaApi api;

  public MainTab(MontoyaApi api) {

    this.api = api;
    requetsPathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.REQUEST_SCRIPT_PATH));

    responsePathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.RESPONSE_SCRIPT_PATH));

    RequestFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(
            new FileNameExtensionFilter(
            "Python, JavaScript files",
            "py", "js"));
        api.persistence().extensionData().setString(Constants.REQUEST_SCRIPT_PATH, path);
        requetsPathLabel.setText(path);
      }
    });

    selectResponseScriptButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(
            new FileNameExtensionFilter(
                "Python, JavaScript files",
                "py", "js"));
        api.persistence().extensionData().setString(Constants.RESPONSE_SCRIPT_PATH, path);
        responsePathLabel.setText(path);
      }
    });
  }

  private String openChooser(
      FileNameExtensionFilter filter
  ) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(filter);
    int response = fileChooser.showOpenDialog(null);

    if (response == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile().getAbsolutePath();
    }

    return "";
  }

  public void setScopeList(
      PersistedList<String> scopeListArray
  ) {
    scopeList.setListData(scopeListArray.toArray());
  }

  public void setBlackList(
      PersistedList<String> scopeListArray
  ) {
    blackList.setListData(scopeListArray.toArray());
  }

  public void setForceIntercept(
      PersistedList<String> scopeListArray
  ) {
    forceInterceptList.setListData(scopeListArray.toArray());
  }
}
