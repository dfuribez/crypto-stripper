import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class MainTab {
  public JPanel panel1;
  private JPanel encryptorsPanel;
  public JCheckBox requestCheckBox;
  public JCheckBox responseCheckBox;
  public JCheckBox forceInterceptInScopeCheckbox;
  private JList scopeList;
  private JList blackList;
  private JList forceInterceptList;
  private JButton clearScopeButton;
  private JButton deleteSelectedScopeButton;
  private JButton deleteSelectedBlacklistButton;
  private JButton deleteSelectedForceButton;
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
  private JButton exportScopeButton;
  private JButton importScopeButton;
  private JButton chooseNodeGlobalBinaryButton;
  private JButton choosePythonGlobalBinaryButton;
  private JPanel globalBinariesPanel;
  private JLabel globalNodeLabel;
  private JLabel globalPythonLabel;


  MontoyaApi api;
  PersistedList<String> stripperScope;
  PersistedList<String> stripperBlackList;
  PersistedList<String> stripperforce;

  public MainTab(
      MontoyaApi api,
      PersistedList<String> stripperScope,
      PersistedList<String> stripperBlackList,
      PersistedList<String> stripperForce
  ) {
    this.api = api;
    this.stripperBlackList = stripperBlackList;
    this.stripperforce = stripperForce;
    this.stripperScope = stripperScope;

    requetsPathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.REQUEST_SCRIPT_PATH_KEY));

    responsePathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.RESPONSE_SCRIPT_PATH_KEY));

    nodePathLabel.setText(
        api.persistence().extensionData().getString(
          Constants.PROJECT_NODE_PATH_KEY
        )
    );

    pythonPathLabel.setText(
        api.persistence().extensionData().getString(
            Constants.PROJECT_PYTHON_PATH_KEY
        )
    );

    globalNodeLabel.setText(
        api.persistence().preferences().getString(
            Constants.GLOBAL_NODE_PATH_KEY
        )
    );

    globalPythonLabel.setText(
        api.persistence().preferences().getString(
            Constants.GLOBAL_PYTHON_PATH_KEY
        )
    );

    loadCurrentSettings();

    this.encryptorsPanel.setBorder(new TitledBorder("Encryptors"));
    this.pathsPanel.setBorder(new TitledBorder("Project Paths"));
    this.globalBinariesPanel.setBorder(new TitledBorder("Global Paths"));


    RequestFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(
            new FileNameExtensionFilter(
            "Python, JavaScript files",
            "py", "js"));

        if (path.isBlank()) {
          return;
        }

        api.persistence().extensionData().setString(
            Constants.REQUEST_SCRIPT_PATH_KEY, path);
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

        if (path.isBlank()) {
          return;
        }

        api.persistence().extensionData().setString(
            Constants.RESPONSE_SCRIPT_PATH_KEY, path);
        responsePathLabel.setText(path);
      }
    });

    chooseNodeBinaryButto.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(null);

        if (!path.isEmpty()) {
          api.persistence().extensionData().setString(
              Constants.PROJECT_NODE_PATH_KEY, path);
          nodePathLabel.setText(path);
        }
      }
    });

    choosePythonBinaryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(null);

        if (!path.isEmpty()) {
          api.persistence().extensionData().setString(
              Constants.PROJECT_PYTHON_PATH_KEY, path);
          pythonPathLabel.setText(path);
        }
      }
    });
    setNodeDefaultButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        api.persistence().extensionData().setString(
            Constants.PROJECT_NODE_PATH_KEY, "node");
        nodePathLabel.setText("node");
      }
    });
    setPythonDefaultButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        api.persistence().extensionData().setString(
            Constants.PROJECT_PYTHON_PATH_KEY, "python");
        pythonPathLabel.setText("python");
      }
    });
    deleteSelectedScopeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("scope");
      }
    });
    deleteSelectedBlacklistButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("blacklist");
      }
    });
    deleteSelectedForceButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("force");
      }
    });
    chooseNodeGlobalBinaryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(null);

        if (path.isBlank()) {
          return;
        }

        api.persistence().preferences().setString(
            Constants.GLOBAL_NODE_PATH_KEY, path);
        globalNodeLabel.setText(path);
      }
    });
    choosePythonGlobalBinaryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(null);

        if (path.isBlank()) {
          return;
        }

        api.persistence().preferences().setString(
            Constants.GLOBAL_PYTHON_PATH_KEY, path);
        globalPythonLabel.setText(path);
      }
    });
    requestCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        saveCurrentSettings();
      }
    });
    responseCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        saveCurrentSettings();
      }
    });
    forceInterceptInScopeCheckbox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        saveCurrentSettings();
      }
    });
    clearScopeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        clearSettings();
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

  public void clearSettings() {
    boolean enableRequest = true;
    boolean enableResponse = true;
    boolean forceInterceptionScope = false;

    this.api.persistence().extensionData().setBoolean(
        Constants.REQUEST_CHECKBOX_STATUS_KEY, enableRequest
    );

    this.api.persistence().extensionData().setBoolean(
        Constants.RESPONSE_CHECKBOX_STATUS_KEY, enableResponse
    );

    this.api.persistence().extensionData().setBoolean(
        Constants.FORCE_CHECKBOX_STATUS_KEY, forceInterceptionScope
    );

    this.api.persistence().extensionData().setString(
        Constants.RESPONSE_SCRIPT_PATH_KEY, ""
    );

    this.api.persistence().extensionData().setString(
        Constants.REQUEST_SCRIPT_PATH_KEY, ""
    );

    this.api.persistence().extensionData().setStringList(
        Constants.STRIPPER_SCOPE_LIST_KEY, PersistedList.persistedStringList()
    );

    this.api.persistence().extensionData().setStringList(
        Constants.STRIPPER_BLACK_LIST_KEY, PersistedList.persistedStringList()
    );

    this.api.persistence().extensionData().setStringList(
        Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY, PersistedList.persistedStringList()
    );

    this.stripperBlackList = PersistedList.persistedStringList();
    this.stripperforce = PersistedList.persistedStringList();
    this.stripperScope = PersistedList.persistedStringList();

    loadCurrentSettings();
  }

  public void loadCurrentSettings() {

    Boolean requestStatus = this.api.persistence().extensionData().getBoolean(
        Constants.REQUEST_CHECKBOX_STATUS_KEY
    );

    Boolean responseStatus = this.api.persistence().extensionData().getBoolean(
        Constants.RESPONSE_CHECKBOX_STATUS_KEY
    );

    Boolean forceStatus = this.api.persistence().extensionData().getBoolean(
        Constants.FORCE_CHECKBOX_STATUS_KEY
    );

    requestStatus = requestStatus == null ? true : requestStatus;
    responseStatus = responseStatus == null ? true : responseStatus;
    forceStatus = forceStatus == null ? false : forceStatus;

    requestCheckBox.setSelected(requestStatus);
    responseCheckBox.setSelected(responseStatus);
    forceInterceptInScopeCheckbox.setSelected(forceStatus);
    setScopeList("scope", this.stripperScope);
    setScopeList("blacklist", this.stripperBlackList);
    setScopeList("force", this.stripperforce);
  }

  public void saveCurrentSettings() {
    boolean requestCheckboxStatus = requestCheckBox.isSelected();
    boolean responseCheckboxStatus = responseCheckBox.isSelected();
    boolean forceCheckboxStatus = forceInterceptInScopeCheckbox.isSelected();

    this.api.persistence().extensionData().setBoolean(
        Constants.FORCE_CHECKBOX_STATUS_KEY,
        forceCheckboxStatus);
    this.api.persistence().extensionData().setBoolean(
        Constants.REQUEST_CHECKBOX_STATUS_KEY,
        requestCheckboxStatus);
    this.api.persistence().extensionData().setBoolean(
        Constants.RESPONSE_CHECKBOX_STATUS_KEY,
        responseCheckboxStatus);
  }

  private void updateScope(
      String source
  ) {
    JList target;
    PersistedList<String> array;
    String key;

    switch (source) {
      case "scope":
        target = this.scopeList;
        array = this.stripperScope;
        key = Constants.STRIPPER_SCOPE_LIST_KEY;
        break;
      case "blacklist":
        target = this.blackList;
        array = this.stripperBlackList;
        key = Constants.STRIPPER_BLACK_LIST_KEY;
        break;
      case "force":
        target = this.forceInterceptList;
        array = this.stripperforce;
        key = Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY;
        break;
      default:
        return;
    }

    DefaultListModel  model = (DefaultListModel) target.getModel();

    int selectedIndex = target.getSelectedIndex();

    Object selectedValue = target.getSelectedValue();

    if (selectedIndex != -1) {
      model.remove(selectedIndex);
      array.remove(selectedValue.toString());
      this.api.persistence().extensionData().deleteString(key);
    }
  }

  public void setScopeList(
      String type,
      PersistedList<String> scopeListArray
  ) {

    DefaultListModel<String> listModel =
        new DefaultListModel<String>();

    listModel.addAll(scopeListArray);

    switch (type) {
      case "scope":
        this.scopeList.setModel(listModel);
        break;
      case "blacklist":
        this.blackList.setModel(listModel);
        break;
      case "force":
        this.forceInterceptList.setModel(listModel);
        break;
    }

  }
}
