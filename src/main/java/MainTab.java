import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
  private JPanel pathsPanel;
  private JButton chooseNodeBinaryButto;
  private JButton choosePythonBinaryButton;
  private JButton setNodeDefaultButton;
  private JButton setPythonDefaultButton;
  private JLabel nodePathLabel;
  private JLabel pythonPathLabel;


  MontoyaApi api;

  public MainTab(MontoyaApi api) {

    this.api = api;
    requetsPathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.REQUEST_SCRIPT_PATH));

    responsePathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.RESPONSE_SCRIPT_PATH));

    nodePathLabel.setText(
        api.persistence().extensionData().getString(
          Constants.PERSISTANCE_NODE_PATH
        )
    );

    pythonPathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.PERSISTANCE_PYTHON_PATH
        )
    );

    this.encryptorsPanel.setBorder(new TitledBorder("Encryptors"));
    this.pathsPanel.setBorder(new TitledBorder("Paths"));


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

    chooseNodeBinaryButto.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(null);

        if (!path.isEmpty()) {
          api.persistence().extensionData().setString(Constants.PERSISTANCE_NODE_PATH, path);
          nodePathLabel.setText(path);
        }
      }
    });

    choosePythonBinaryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(null);

        if (!path.isEmpty()) {
          api.persistence().extensionData().setString(Constants.PERSISTANCE_PYTHON_PATH, path);
          pythonPathLabel.setText(path);
        }
      }
    });
    setNodeDefaultButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        api.persistence().extensionData().setString(Constants.PERSISTANCE_NODE_PATH, "node");
        nodePathLabel.setText("node");
      }
    });
    setPythonDefaultButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        api.persistence().extensionData().setString(Constants.PERSISTANCE_PYTHON_PATH, "python");
        pythonPathLabel.setText("python");
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
