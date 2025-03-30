import burp.api.montoya.persistence.PersistedList;

import javax.swing.*;

public class MainTab {
  public JPanel panel1;
  private JPanel encryptorsPanel;
  private JCheckBox requestCheckBox;
  private JCheckBox responseCheckBox;
  public JCheckBox interceptionScopeCheckBox;
  private JList scopeList;
  private JList blackList;
  private JList forceInterceptList;
  private JButton button1;
  private JButton deleteSelectedButton;
  private JButton deleteSelectedButton1;
  private JButton deleteSelectedButton2;


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
