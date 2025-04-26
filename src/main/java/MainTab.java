import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
  private JButton restoreSettingsButton;
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
  private JButton importSettingsButton;
  private JButton chooseNodeGlobalBinaryButton;
  private JButton choosePythonGlobalBinaryButton;
  private JPanel globalBinariesPanel;
  private JLabel globalNodeLabel;
  private JLabel globalPythonLabel;
  private JPanel scopeListPanel;
  private JPanel blackListPanel;
  private JPanel forceInterceptListPanel;
  private JButton JSTemplateButton;
  private JButton pythonTemplateButton;
  private JTextField scopeUrlTextField;
  private JButton addScopeUrlButton;
  private JTextField blackListUrlTextField;
  private JButton addBlackListUrlButton;
  private JTextField forceUrlTextField;
  private JButton addForceUrlButton;

  MontoyaApi api;


  public MainTab(MontoyaApi api) {
    this.api = api;

    loadCurrentSettings();

    this.encryptorsPanel.setBorder(new TitledBorder("Transformers:"));
    this.pathsPanel.setBorder(new TitledBorder("Project Paths:"));
    this.globalBinariesPanel.setBorder(new TitledBorder("Global Paths:"));

    this.scopeListPanel.setBorder(new TitledBorder("Scope:"));
    this.blackListPanel.setBorder(new TitledBorder("Black List"));
    this.forceInterceptListPanel.setBorder(new TitledBorder("Force intercept:"));


    RequestFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Python, JavaScript files",
            "py", "js");
        String path = openChooser(filter, true);

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
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Python, JavaScript files",
            "py", "js");
        String path = openChooser(filter, true);

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
        String path = openChooser(null, true);

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
        String path = openChooser(null, true);

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
            Constants.PROJECT_NODE_PATH_KEY, "");
        nodePathLabel.setText("");
      }
    });
    setPythonDefaultButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        api.persistence().extensionData().setString(
            Constants.PROJECT_PYTHON_PATH_KEY, "");
        pythonPathLabel.setText("");
      }
    });
    deleteSelectedScopeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("scope", "delete");
      }
    });
    deleteSelectedBlacklistButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("blacklist", "delete");
      }
    });
    deleteSelectedForceButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("force", "delete");
      }
    });
    chooseNodeGlobalBinaryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        String path = openChooser(null, true);

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
        String path = openChooser(null, true);

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
    restoreSettingsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        clearSettings();
      }
    });
    exportScopeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        exportSettings();
      }
    });
    importSettingsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        importSettings();
      }
    });
    JSTemplateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "JavaScript files", "js");
        String path = openChooser(filter, false);
        if (path.isBlank()) {
          return;
        }
        try (Writer writer = new FileWriter(path)) {
          writer.write(Constants.JS_TEMPLATE);
        } catch (Exception e) {
          api.logging().logToError(e.toString());
        }
      }
    });
    pythonTemplateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Python files", "py");
        String path = openChooser(filter, false);
        if (path.isBlank()) {
          return;
        }
        try (Writer writer = new FileWriter(path)) {
          writer.write(Constants.PYTHON_TEMPLATE);
        } catch (Exception e) {
          api.logging().logToError(e.toString());
        }
      }
    });
    addScopeUrlButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("scope", "add");
        scopeUrlTextField.setText("");

      }
    });
    addBlackListUrlButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("blacklist", "add");
        blackListUrlTextField.setText("");
      }
    });
    addForceUrlButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        updateScope("force", "add");
        forceUrlTextField.setText("");
      }
    });
  }

  private void importSettings() {
    String path = openChooser(
        new FileNameExtensionFilter("JSON, configuration files", "json"),
        true);

    if (path.isBlank()) {
      return;
    }

    try (Reader reader = new FileReader(path)) {
      Gson gson = new Gson();
      JsonSettings jsonSettings = gson.fromJson(reader, JsonSettings.class);

      api.persistence().extensionData().setBoolean(
          Constants.REQUEST_CHECKBOX_STATUS_KEY, jsonSettings.isEnableRequest()
      );
      api.persistence().extensionData().setBoolean(
          Constants.RESPONSE_CHECKBOX_STATUS_KEY, jsonSettings.isEnableResponse()
      );
      api.persistence().extensionData().setBoolean(
          Constants.FORCE_CHECKBOX_STATUS_KEY, jsonSettings.isEnableForceIntercept()
      );

      api.persistence().extensionData().setStringList(
          Constants.STRIPPER_SCOPE_LIST_KEY,
          Utils.arrayToPersisted(jsonSettings.getScope())
      );
      api.persistence().extensionData().setStringList(
          Constants.STRIPPER_BLACK_LIST_KEY,
          Utils.arrayToPersisted(jsonSettings.getBlackList())
      );
      api.persistence().extensionData().setStringList(
          Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY,
          Utils.arrayToPersisted(jsonSettings.getForceIntercept())
      );
      loadCurrentSettings();

    } catch (Exception e) {
      api.logging().logToError(e.toString());
    }

  }

  private void exportSettings() {
    JsonSettings settings = new JsonSettings();

    settings.setEnableRequest(
        api.persistence().extensionData().getBoolean(
            Constants.REQUEST_CHECKBOX_STATUS_KEY
        )
    );
    settings.setEnableResponse(
        api.persistence().extensionData().getBoolean(
            Constants.RESPONSE_CHECKBOX_STATUS_KEY
        )
    );
    settings.setEnableForceIntercept(
        api.persistence().extensionData().getBoolean(
            Constants.FORCE_CHECKBOX_STATUS_KEY
        )
    );

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    settings.setBlackList(scope.get("blacklist").toArray(new String[0]));
    settings.setScope(scope.get("scope").toArray(new String[0]));
    settings.setForceIntercept(scope.get("force").toArray(new String[0]));

    FileNameExtensionFilter filter =
        new FileNameExtensionFilter("JSON, configuration files", "json");

    String path = openChooser(filter, false);
    try (Writer writer = new FileWriter(path, StandardCharsets.UTF_8)) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(settings, writer);
    } catch (Exception e) {
      this.api.logging().logToError(e.toString());
    }
  }

  private String openChooser(FileNameExtensionFilter filter, boolean isOpenDialog) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(filter);

    int response;

    if (isOpenDialog) {
      response = fileChooser.showOpenDialog(api.userInterface().swingUtils().suiteFrame());
    } else {
      response = fileChooser.showSaveDialog(api.userInterface().swingUtils().suiteFrame());
    }

    if (response == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile().getAbsolutePath();
    }

    return "";
  }

  public void clearSettings() {
    boolean enableRequest = true;
    boolean enableResponse = true;
    boolean forceInterceptionScope = false;

    api.persistence().extensionData().setBoolean(
        Constants.REQUEST_CHECKBOX_STATUS_KEY, enableRequest
    );

    api.persistence().extensionData().setBoolean(
        Constants.RESPONSE_CHECKBOX_STATUS_KEY, enableResponse
    );

    api.persistence().extensionData().setBoolean(
        Constants.FORCE_CHECKBOX_STATUS_KEY, forceInterceptionScope
    );

    api.persistence().extensionData().setString(
        Constants.RESPONSE_SCRIPT_PATH_KEY, ""
    );

    api.persistence().extensionData().setString(
        Constants.REQUEST_SCRIPT_PATH_KEY, ""
    );

    api.persistence().extensionData().setStringList(
        Constants.STRIPPER_SCOPE_LIST_KEY, PersistedList.persistedStringList()
    );

    api.persistence().extensionData().setStringList(
        Constants.STRIPPER_BLACK_LIST_KEY, PersistedList.persistedStringList()
    );

    api.persistence().extensionData().setStringList(
        Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY, PersistedList.persistedStringList()
    );

    loadCurrentSettings();
  }

  public void loadCurrentSettings() {
    Boolean requestStatus = api.persistence().extensionData().getBoolean(
        Constants.REQUEST_CHECKBOX_STATUS_KEY
    );

    Boolean responseStatus = api.persistence().extensionData().getBoolean(
        Constants.RESPONSE_CHECKBOX_STATUS_KEY
    );

    Boolean forceStatus = api.persistence().extensionData().getBoolean(
        Constants.FORCE_CHECKBOX_STATUS_KEY
    );

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    requestStatus = requestStatus == null ? true : requestStatus;
    responseStatus = responseStatus == null ? true : responseStatus;
    forceStatus = forceStatus == null ? false : forceStatus;

    requestCheckBox.setSelected(requestStatus);
    responseCheckBox.setSelected(responseStatus);
    forceInterceptInScopeCheckbox.setSelected(forceStatus);
    setScopeList("scope", scope.get("scope"));
    setScopeList("blacklist", scope.get("blacklist"));
    setScopeList("force", scope.get("force"));

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

  private void updateScope(String source, String action) {
    JList target;
    PersistedList<String> selectedScopeList;
    String key;
    String addUrl;

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    switch (source) {
      case "scope":
        target = this.scopeList;
        selectedScopeList = scope.get("scope");
        key = Constants.STRIPPER_SCOPE_LIST_KEY;
        addUrl = scopeUrlTextField.getText();
        break;
      case "blacklist":
        target = this.blackList;
        selectedScopeList = scope.get("blacklist");
        key = Constants.STRIPPER_BLACK_LIST_KEY;
        addUrl = blackListUrlTextField.getText();
        break;
      case "force":
        target = this.forceInterceptList;
        selectedScopeList = scope.get("force");
        key = Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY;
        addUrl = forceUrlTextField.getText();
        break;
      default:
        return;
    }

    if (action == "delete") {
      DefaultListModel  model = (DefaultListModel) target.getModel();

      int selectedIndex = target.getSelectedIndex();

      Object selectedValue = target.getSelectedValue();

      if (selectedIndex != -1) {
        model.remove(selectedIndex);
        selectedScopeList.remove(selectedValue.toString());
      }
    } else {

      if (!Utils.isValidRegex(addUrl) || addUrl.isBlank()) {
        showAlertMessage("Please check your url, it is not a valid regex");
        return;
      }

      if (Utils.isUrlInScope(addUrl, selectedScopeList)) {
        showAlertMessage("Url already in the scope");
        return;
      }

      selectedScopeList.add(addUrl);
      setScopeList(source, selectedScopeList);
    }
    api.persistence().extensionData().setStringList(key, selectedScopeList);

  }

  public void setScopeList(String type, PersistedList<String> scopeListArray) {
    DefaultListModel<String> listModel = new DefaultListModel<String>();
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

  private void showAlertMessage(String message) {
    JOptionPane.showMessageDialog(
        api.userInterface().swingUtils().suiteFrame(), message);
  }
}
