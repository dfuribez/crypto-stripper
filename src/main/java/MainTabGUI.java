import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.JsonSettings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainTabGUI {
  public JPanel panel1 = new JPanel(new MigLayout("fillx"));
  private JPanel encryptorsPanel = new JPanel(new MigLayout());
  public JCheckBox requestCheckBox = new JCheckBox("Request:");
  public JCheckBox responseCheckBox = new JCheckBox("Response:");
  public JCheckBox forceInterceptInScopeCheckbox = new JCheckBox("Force Intercept In Scope");

  private JList<String> scopeList = new JList<>();
  private JList<String> blackList = new JList<>();
  private JList<String> forceInterceptList = new JList<>();

  private JButton restoreSettingsButton = new JButton("Restore Settings");
  private JButton deleteSelectedScopeButton = new JButton("Delete selection");
  private JButton deleteSelectedBlacklistButton = new JButton("Delete selection");
  private JButton deleteSelectedForceButton = new JButton("Delete selection");

  private JButton selectRequestScriptButton = new JButton("Select Request Script");
  private JButton selectResponseScriptButton = new JButton("Select Response Script");

  private JPanel pathsPanel = new JPanel(new MigLayout());
  private JButton chooseNodeBinaryButton = new JButton("Choose Node");
  private JButton choosePythonBinaryButton = new JButton("Choose Python");
  private JButton setNodeDefaultButton = new JButton("X");
  private JButton setPythonDefaultButton = new JButton("X");


  private JTextField nodePathTextField = new JTextField();
  private JTextField pythonPathTextField = new JTextField();

  private JButton exportSettingsButton = new JButton("Export Settings");
  private JButton importSettingsButton = new JButton("Import Settings");

  private JButton chooseNodeGlobalBinaryButton = new JButton("Global Node");
  private JButton choosePythonGlobalBinaryButton = new JButton("Global Python");

  private JPanel globalBinariesPanel = new JPanel(new MigLayout());
  private JTextField globalNodeLabel = new JTextField("Global Node");
  private JTextField globalPythonLabel = new JTextField("Global Python");

  private JPanel scopeListPanel = new JPanel(new MigLayout());
  private JPanel blackListPanel = new JPanel(new MigLayout());
  private JPanel forceListPanel = new JPanel(new MigLayout());

  private JButton JSTemplateButton = new JButton("JS Template");
  private JButton pythonTemplateButton = new JButton("Python Template");

  private JTextField scopeUrlTextField = new JTextField();
  private JButton addScopeUrlButton = new JButton("Add");

  private JTextField blackListUrlTextField = new JTextField();
  private JButton addBlackListUrlButton = new JButton("Add");

  private JTextField forceUrlTextField = new JTextField();
  private JButton addForceUrlButton = new JButton("Add");

  private JTextArea versionTextArea = new JTextArea(5, 20);

  public JCheckBox enableBlackListcheckbox = new JCheckBox("Enable");
  public JCheckBox enableForceinterceptCheckbox = new JCheckBox("Enable");

  private JTextField requetsPathLabel = new JTextField();
  private JTextField responsePathLabel = new JTextField();

  private JButton openRequestButton = new JButton("Open");
  private JButton openResponseButton = new JButton("Open");

  private JScrollPane scrollScope = new JScrollPane(scopeList);
  private JScrollPane scrollBlackList = new JScrollPane(blackList);
  private JScrollPane scrollForceInterceptList = new JScrollPane(forceInterceptList);

  public JCheckBox enableForceCheckbox = new JCheckBox("Enable Force");

  MontoyaApi api;
  JFileChooser fileChooser = new JFileChooser();

  public MainTabGUI(MontoyaApi api) {
    this.api = api;

    loadCurrentSettings();
    setAltText();

    Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    pathsPanel.setBorder(BorderFactory.createCompoundBorder(
        new TitledBorder(BorderFactory.createEtchedBorder(), "Project paths:"),
        emptyBorder
    ));

    globalBinariesPanel.setBorder(BorderFactory.createCompoundBorder(
        new TitledBorder(BorderFactory.createEtchedBorder(), "Global paths:"),
        emptyBorder
    ));

    versionTextArea.setText(Constants.VERSION);

    selectRequestScriptButton.addActionListener(e -> {
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
          "Python, JavaScript files",
          "py", "js");
      String path = openChooser("Request script", filter, true);

      if (path.isBlank()) {
        return;
      }

      api.persistence().extensionData().setString(
          Constants.REQUEST_SCRIPT_PATH_KEY, path);
      requetsPathLabel.setText(path);
    });

    selectResponseScriptButton.addActionListener(e -> {
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
          "Python, JavaScript files",
          "py", "js");
      String path = openChooser("Response script", filter, true);

      if (path.isBlank()) {
        return;
      }

      api.persistence().extensionData().setString(Constants.RESPONSE_SCRIPT_PATH_KEY, path);
      responsePathLabel.setText(path);
    });

    chooseNodeBinaryButton.addActionListener(e -> {
      String path = openChooser("Node binary path", null, true);

      if (!path.isEmpty()) {
        api.persistence().extensionData().setString(Constants.PROJECT_NODE_PATH_KEY, path);
        nodePathTextField.setText(path);
      }
    });

    choosePythonBinaryButton.addActionListener(e -> {
      String path = openChooser("Python binary path", null, true);

      if (!path.isEmpty()) {
        api.persistence().extensionData().setString(Constants.PROJECT_PYTHON_PATH_KEY, path);
        pythonPathTextField.setText(path);
      }
    });
    setNodeDefaultButton.addActionListener(e -> {
      api.persistence().extensionData().setString(Constants.PROJECT_NODE_PATH_KEY, "");
      nodePathTextField.setText("");
    });
    setPythonDefaultButton.addActionListener(e -> {
      api.persistence().extensionData().setString(Constants.PROJECT_PYTHON_PATH_KEY, "");
      pythonPathTextField.setText("");
    });

    deleteSelectedScopeButton.addActionListener(e -> updateScope("scope", "delete"));
    deleteSelectedBlacklistButton.addActionListener(e -> updateScope("blacklist", "delete"));
    deleteSelectedForceButton.addActionListener(e -> updateScope("force", "delete"));
    chooseNodeGlobalBinaryButton.addActionListener(e -> {
      String path = openChooser("Node binary path", null, true);

      if (path.isBlank()) {
        return;
      }

      api.persistence().preferences().setString(Constants.GLOBAL_NODE_PATH_KEY, path);
      globalNodeLabel.setText(path);
    });
    choosePythonGlobalBinaryButton.addActionListener(e -> {
      String path = openChooser("Python binary path",null, true);

      if (path.isBlank()) {
        return;
      }

      api.persistence().preferences().setString(Constants.GLOBAL_PYTHON_PATH_KEY, path);
      globalPythonLabel.setText(path);
    });
    requestCheckBox.addActionListener(e -> saveCurrentSettings());
    responseCheckBox.addActionListener(e -> saveCurrentSettings());
    forceInterceptInScopeCheckbox.addActionListener(e -> saveCurrentSettings());
    restoreSettingsButton.addActionListener(e -> clearSettings());
    exportSettingsButton.addActionListener(e -> exportSettings());
    importSettingsButton.addActionListener(e -> importSettings());
    JSTemplateButton.addActionListener(e -> {
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
          "JavaScript files", "js");
      String path = openChooser("JS template", filter, false);
      if (path.isBlank()) {
        return;
      }
      Utils.resourceToFile(api, "template.js", path);
    });
    pythonTemplateButton.addActionListener(e -> {
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
          "Python files", "py");
      String path = openChooser("Python template", filter, false);
      if (path.isBlank()) {
        return;
      }

      Utils.resourceToFile(api, "template.py", path);

    });
    addScopeUrlButton.addActionListener(e -> updateScope("scope", "add"));
    addBlackListUrlButton.addActionListener(e -> updateScope("blacklist", "add"));
    addForceUrlButton.addActionListener(e -> updateScope("force", "add"));
    openRequestButton.addActionListener(e -> Utils.openFolder(requetsPathLabel.getText()));
    openResponseButton.addActionListener(e -> Utils.openFolder(responsePathLabel.getText()));

    initializeUI();
    setLayout();
  }

  private void initializeUI() {
    nodePathTextField.setEditable(false);
    pythonPathTextField.setEditable(false);

    requetsPathLabel.setEditable(false);
    responsePathLabel.setEditable(false);

    versionTextArea.setEditable(false);

    globalNodeLabel.setEditable(false);
    globalPythonLabel.setEditable(false);
  }

  private void setLayout() {
    encryptorsPanel.add(requestCheckBox);
    encryptorsPanel.add(requetsPathLabel, "growx, pushx");
    encryptorsPanel.add(selectRequestScriptButton, "sg btn");
    encryptorsPanel.add(openRequestButton, "sg btn1, wrap");
    encryptorsPanel.add(responseCheckBox);
    encryptorsPanel.add(responsePathLabel, "growx, pushx");
    encryptorsPanel.add(selectResponseScriptButton, "sg btn");
    encryptorsPanel.add(openResponseButton, "sg btn1, wrap");
    encryptorsPanel.add(forceInterceptInScopeCheckbox);

    panel1.add(encryptorsPanel, "growx, wrap");

    // --------
    JPanel gpathsPanel = new JPanel(new MigLayout("", "[50%][50%]", ""));

    // Global binaries
    globalBinariesPanel.add(new JLabel("Node:"));
    globalBinariesPanel.add(globalNodeLabel, "sg lbl, growx, pushx");
    globalBinariesPanel.add(chooseNodeGlobalBinaryButton, "sg btn, align right, wrap");
    globalBinariesPanel.add(new JLabel("Python:"));
    globalBinariesPanel.add(globalPythonLabel, "growx, pushx");
    globalBinariesPanel.add(choosePythonGlobalBinaryButton, "sg btn, align right, wrap");

    gpathsPanel.add(globalBinariesPanel, "sgy a, growx, pushx");

    // Project Paths
    pathsPanel.add(new JLabel("Node:"));
    pathsPanel.add(nodePathTextField, "sg lbl, growx, pushx");
    pathsPanel.add(chooseNodeBinaryButton, "sg btn");
    pathsPanel.add(setNodeDefaultButton, "sg btn1, wrap");
    pathsPanel.add(new JLabel("Python:"));
    pathsPanel.add(pythonPathTextField, "sg lbl, growx, pushx");
    pathsPanel.add(choosePythonBinaryButton, "sg btn");
    pathsPanel.add(setPythonDefaultButton, "sg btn1");

    gpathsPanel.add(pathsPanel, "sgy a, growx, pushx");

    panel1.add(gpathsPanel, "growx, wrap");

    // ---------------
    JPanel separator = Utils.separator("Crypto Stripper scope", "center", true);

    panel1.add(separator, "growx, wrap");

    // ------
    JPanel scopesPanel = new JPanel(new MigLayout("fill"));

    // Scope panel
    scopeListPanel.add(Utils.separator("Scope (?)", "center", false), "span, growx");
    scopeListPanel.add(scrollScope, "wrap, span 2, grow, push");
    scopeListPanel.add(deleteSelectedScopeButton, "alignx right, span 2, wrap");
    scopeListPanel.add(scopeUrlTextField, "growx, pushx");
    scopeListPanel.add(addScopeUrlButton);

    scopesPanel.add(scopeListPanel, "sg pn, grow");

    // BlackList panel
    blackListPanel.add(Utils.separator("Black List: (?)", "center", false), "span, growx");
    blackListPanel.add(scrollBlackList, "span 2, grow, push, wrap");
    blackListPanel.add(enableBlackListcheckbox, "alignx left");
    blackListPanel.add(deleteSelectedBlacklistButton, "alignx right, wrap");

    JPanel addBlackListPanel = new JPanel(new MigLayout("insets 0"));

    addBlackListPanel.add(blackListUrlTextField, "growx, pushx");
    addBlackListPanel.add(addBlackListUrlButton);

    blackListPanel.add(addBlackListPanel, "span, growx");

    scopesPanel.add(blackListPanel, "sg pn, grow");

    // Force interept panel
    forceListPanel.add(Utils.separator("Force intercept: (?)", "center", false), "span, growx");

    forceListPanel.add(scrollForceInterceptList, "wrap, span 2, grow, push");
    forceListPanel.add(enableForceinterceptCheckbox, "alignx left");
    forceListPanel.add(deleteSelectedForceButton, "alignx right, wrap");

    JPanel addForcePanel = new JPanel(new MigLayout("insets 0"));

    addForcePanel.add(forceUrlTextField, "growx, pushx");
    addForcePanel.add(addForceUrlButton);

    forceListPanel.add(addForcePanel, "span, growx");

    scopesPanel.add(forceListPanel, "sg pn, grow");

    panel1.add(scopesPanel, "grow, wrap");

    // Buttons
    JPanel buttonsPanel = new JPanel(new MigLayout());

    buttonsPanel.add(JSTemplateButton);
    buttonsPanel.add(pythonTemplateButton);
    buttonsPanel.add(new JPanel(), "pushx, growx");
    buttonsPanel.add(restoreSettingsButton);
    buttonsPanel.add(new JSeparator(SwingConstants.VERTICAL), "width 2!, growy");
    buttonsPanel.add(exportSettingsButton);
    buttonsPanel.add(importSettingsButton);

    panel1.add(new JPanel(), "pushy, wrap");
    panel1.add(versionTextArea, "wrap");
    panel1.add(buttonsPanel, "growx");
  }


  private void importSettings() {
    String path = openChooser(
        "Import JSON settings",
        new FileNameExtensionFilter("JSON, configuration files", "json"),
        true
    );

    if (path.isBlank()) {
      return;
    }

    try (Reader reader = new FileReader(path)) {
      Gson gson = new Gson();
      JsonSettings jsonSettings = gson.fromJson(reader, JsonSettings.class);

      api.persistence().extensionData().setBoolean(
          Constants.REQUEST_CHECKBOX_STATUS_KEY, jsonSettings.enableRequest);
      api.persistence().extensionData().setBoolean(
          Constants.RESPONSE_CHECKBOX_STATUS_KEY, jsonSettings.enableResponse);
      api.persistence().extensionData().setBoolean(
          Constants.FORCE_CHECKBOX_STATUS_KEY, jsonSettings.enableForceIntercept);
      api.persistence().extensionData().setStringList(
          Constants.STRIPPER_SCOPE_LIST_KEY,
          Utils.arrayToPersisted(cleanScope(jsonSettings.scope)));
      api.persistence().extensionData().setStringList(
          Constants.STRIPPER_BLACK_LIST_KEY,
          Utils.arrayToPersisted(cleanScope(jsonSettings.blackList)));
      api.persistence().extensionData().setStringList(
          Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY,
          Utils.arrayToPersisted(cleanScope(jsonSettings.forceIntercept)));
      loadCurrentSettings();
    } catch (Exception e) {
      api.logging().logToError(e.toString());
    }
  }

  private void exportSettings() {
    JsonSettings settings = new JsonSettings();

    settings.enableRequest =
        api.persistence().extensionData().getBoolean(Constants.REQUEST_CHECKBOX_STATUS_KEY);
    settings.enableResponse =
        api.persistence().extensionData().getBoolean(Constants.RESPONSE_CHECKBOX_STATUS_KEY);
    settings.enableForceIntercept =
        api.persistence().extensionData().getBoolean(Constants.FORCE_CHECKBOX_STATUS_KEY);

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    settings.blackList = scope.get("blacklist").toArray(new String[0]);
    settings.scope = scope.get("scope").toArray(new String[0]);
    settings.forceIntercept = scope.get("force").toArray(new String[0]);

    FileNameExtensionFilter filter =
        new FileNameExtensionFilter("JSON, configuration files", "json");

    String path = openChooser("Export JSON settings", filter, false);
    try (Writer writer = new FileWriter(path, StandardCharsets.UTF_8)) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(settings, writer);
    } catch (Exception e) {
      api.logging().logToError(e.toString());
    }
  }

  private String openChooser(String title, FileNameExtensionFilter filter, boolean isOpenDialog) {
    fileChooser.setDialogTitle(title);
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
    api.persistence().extensionData().setBoolean(Constants.REQUEST_CHECKBOX_STATUS_KEY, true);
    api.persistence().extensionData().setBoolean(Constants.RESPONSE_CHECKBOX_STATUS_KEY, true);
    api.persistence().extensionData().setBoolean(Constants.FORCE_CHECKBOX_STATUS_KEY, false);
    api.persistence().extensionData().setString(Constants.RESPONSE_SCRIPT_PATH_KEY, "");
    api.persistence().extensionData().setString(Constants.REQUEST_SCRIPT_PATH_KEY, "");

    api.persistence().extensionData().setString(Constants.PROJECT_NODE_PATH_KEY, "");
    api.persistence().extensionData().setString(Constants.PROJECT_PYTHON_PATH_KEY, "");

    api.persistence().extensionData().setStringList(
        Constants.STRIPPER_SCOPE_LIST_KEY, PersistedList.persistedStringList());
    api.persistence().extensionData().setStringList(
        Constants.STRIPPER_BLACK_LIST_KEY, PersistedList.persistedStringList());
    api.persistence().extensionData().setStringList(
        Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY, PersistedList.persistedStringList());

    api.persistence().preferences().setString(Constants.GLOBAL_PYTHON_PATH_KEY, "");
    api.persistence().preferences().setString(Constants.GLOBAL_NODE_PATH_KEY, "");
    loadCurrentSettings();
  }

  public void loadCurrentSettings() {
    Boolean requestStatus = api.persistence().extensionData().getBoolean(
        Constants.REQUEST_CHECKBOX_STATUS_KEY);

    Boolean responseStatus = api.persistence().extensionData().getBoolean(
        Constants.RESPONSE_CHECKBOX_STATUS_KEY);

    Boolean forceStatus = api.persistence().extensionData().getBoolean(
        Constants.FORCE_CHECKBOX_STATUS_KEY);

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    requestStatus = requestStatus == null || requestStatus;
    responseStatus = responseStatus == null || responseStatus;
    forceStatus = forceStatus != null && forceStatus;

    requestCheckBox.setSelected(requestStatus);
    responseCheckBox.setSelected(responseStatus);
    forceInterceptInScopeCheckbox.setSelected(forceStatus);
    setScopeList("scope", scope.get("scope"));
    setScopeList("blacklist", scope.get("blacklist"));
    setScopeList("force", scope.get("force"));

    requetsPathLabel.setText(
        api.persistence().extensionData().getString(Constants.REQUEST_SCRIPT_PATH_KEY));

    responsePathLabel.setText(
        api.persistence().extensionData().getString(Constants.RESPONSE_SCRIPT_PATH_KEY));

    nodePathTextField.setText(
        api.persistence().extensionData().getString(Constants.PROJECT_NODE_PATH_KEY));

    pythonPathTextField.setText(
        api.persistence().extensionData().getString(Constants.PROJECT_PYTHON_PATH_KEY));

    globalNodeLabel.setText(
        api.persistence().preferences().getString(Constants.GLOBAL_NODE_PATH_KEY));

    globalPythonLabel.setText(
        api.persistence().preferences().getString(Constants.GLOBAL_PYTHON_PATH_KEY));
  }

  public void saveCurrentSettings() {
    boolean requestCheckboxStatus = requestCheckBox.isSelected();
    boolean responseCheckboxStatus = responseCheckBox.isSelected();
    boolean forceCheckboxStatus = forceInterceptInScopeCheckbox.isSelected();

    api.persistence().extensionData().setBoolean(
        Constants.FORCE_CHECKBOX_STATUS_KEY, forceCheckboxStatus);
    api.persistence().extensionData().setBoolean(
        Constants.REQUEST_CHECKBOX_STATUS_KEY, requestCheckboxStatus);
    api.persistence().extensionData().setBoolean(
        Constants.RESPONSE_CHECKBOX_STATUS_KEY, responseCheckboxStatus);
  }

  private void updateScope(String source, String action) {
    JList<String> target;
    JTextField selectedTextField;
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
        selectedTextField = scopeUrlTextField;
        break;
      case "blacklist":
        target = this.blackList;
        selectedScopeList = scope.get("blacklist");
        key = Constants.STRIPPER_BLACK_LIST_KEY;
        selectedTextField = blackListUrlTextField;
        break;
      case "force":
        target = this.forceInterceptList;
        selectedScopeList = scope.get("force");
        key = Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY;
        selectedTextField = forceUrlTextField;
        break;
      default:
        return;
    }

    if (Objects.equals(action, "delete")) {
      DefaultListModel model = (DefaultListModel) target.getModel();

      int selectedIndex = target.getSelectedIndex();

      Object selectedValue = target.getSelectedValue();

      if (selectedIndex != -1) {
        model.remove(selectedIndex);
        selectedScopeList.remove(selectedValue.toString());
      }
    } else {
      addUrl = selectedTextField.getText();
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
      selectedTextField.setText("");
    }
    api.persistence().extensionData().setStringList(key, selectedScopeList);

  }

  public void setScopeList(String type, PersistedList<String> scopeListArray) {
    DefaultListModel<String> listModel = new DefaultListModel<>();
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

  private void setAltText() {
    forceInterceptInScopeCheckbox.setToolTipText("Intercept in-scope requests even when the proxy is set to not intercept.");
    requestCheckBox.setToolTipText("Enable Stripper for requests.");
    responseCheckBox.setToolTipText("Enable Stripper for responses.");
    globalBinariesPanel.setToolTipText("Binary paths persist across all projects but are overwritten if a project-specific path is set.");
    pathsPanel.setToolTipText("Project-specific paths will override the Global path for this project only.");
    scopeListPanel.setToolTipText("Any endpoint in this list will trigger Stripper's main functionality.");
    blackListPanel.setToolTipText("Endpoints in this list will be excluded from interception by Burp.");
    forceListPanel.setToolTipText("Endpoints in this list will be intercepted even if the proxy is set to not intercept.");
    addScopeUrlButton.setToolTipText("Add a new endpoint. Regular expressions are supported.");
    addBlackListUrlButton.setToolTipText("Add a new endpoint. Regular expressions are supported.");
    addForceUrlButton.setToolTipText("Add a new endpoint. Regular expressions are supported.");
  }

  private String[] cleanScope(String[] scopeList) {
    ArrayList<String> clean = new ArrayList<>();
    for (String url : scopeList) {
      if (Utils.isValidRegex(url)) {
        clean.add(url);
      }
    }
    return clean.toArray(new String[0]);
  }

  public JPanel getMainPanel() {
    return panel1;
  }
}
