import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class MyContextMenus  implements ContextMenuItemsProvider {
  private final MontoyaApi api;
  private MainTab mainTab;

  public MyContextMenus(MontoyaApi api, MainTab tab) {
    this.api = api;
    this.mainTab = tab;
  }

  public void updateStripperScope(String source, String action, String url) {
    PersistedList<String> target;
    String key;

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    if (!Utils.isValidRegex(url)) {
      url = Pattern.quote(url);
    }

    switch (source) {
      case "blacklist":
        target = scope.get("blacklist");
        key = Constants.STRIPPER_BLACK_LIST_KEY;
        break;
      case "force":
        target = scope.get("force");
        key = Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY;
        break;
      case "scope":
        target = scope.get("scope");
        key = Constants.STRIPPER_SCOPE_LIST_KEY;
        break;
      default:
        return;
    }

    if ("add".equals(action)) {
      target.add(url);
    } else {
      target.remove(url);
    }

    api.persistence().extensionData().setStringList(key, target);
    mainTab.loadCurrentSettings();
  }

  public void decryptRequest(MessageEditorHttpRequestResponse requestResponse){
    HttpRequest request = requestResponse.requestResponse().request();

    HashMap<String, String> preparedToExecute =
        Utils.prepareRequestForExecutor(request, -1);

    ExecutorOutput executorResponse =
        Executor.execute(api, "decrypt", "request", preparedToExecute);

    requestResponse.setRequest(Utils.executorToHttpRequest(request, executorResponse));
  }


  @Override
  public List<Component> provideMenuItems(ContextMenuEvent event) {

    List<Component> menuItemList = new ArrayList<>();
    if (event.messageEditorRequestResponse().isEmpty()) {
      return null;
    }

    MessageEditorHttpRequestResponse requestResponse =
        event.messageEditorRequestResponse().get();

    String url =
        Utils.removeQueryFromUrl(requestResponse.requestResponse().request().url());

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(this.api.persistence().extensionData());

    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER) &&
        event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST)
    ) {

      if (Utils.isUrlInScope(url, scope.get("scope"))) {
        JMenuItem item = new JMenuItem("Decrypt");
        item.addActionListener(
            l -> this.decryptRequest(requestResponse));
        menuItemList.add(item);
      }
    }

    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER)
        && event.isFrom(
            InvocationType.MESSAGE_EDITOR_REQUEST,
            InvocationType.MESSAGE_VIEWER_REQUEST)
    ) {
      if (Utils.isUrlInScope(url, scope.get("scope"))) {
        JMenuItem removeScopeItem = new JMenuItem("Remove from scope");
        removeScopeItem.addActionListener(
            l -> this.updateStripperScope(
                "scope", "remove", url));
        menuItemList.add(removeScopeItem);
      } else {
        JMenuItem item = new JMenuItem("Add url to scope");
        item.addActionListener(
            l -> this.updateStripperScope(
                "scope", "add", url));
        menuItemList.add(item);
      }

      if (Utils.isUrlInScope(url, scope.get("blacklist"))) {
        JMenuItem removeFromBlacklistItem =
            new JMenuItem("Remove endpoint from blacklist");
        removeFromBlacklistItem.addActionListener(
            l -> this.updateStripperScope(
                "blacklist", "remove", url));
        menuItemList.add(removeFromBlacklistItem);
      } else {
        JMenuItem addToBlacklistItem =
            new JMenuItem("Add endpoint to blacklist");
        addToBlacklistItem.addActionListener(
            l -> this.updateStripperScope(
                "blacklist", "add", url));
        menuItemList.add(addToBlacklistItem);
      }

      if (Utils.isUrlInScope(url, scope.get("force"))) {
        JMenuItem removeFromBlacklistItem =
            new JMenuItem("Do not force interception");
        removeFromBlacklistItem.addActionListener(
            l -> this.updateStripperScope(
                "force", "remove", url));
        menuItemList.add(removeFromBlacklistItem);
      } else {
        JMenuItem addToBlacklistItem =
            new JMenuItem("Force interception this endpoint");
        addToBlacklistItem.addActionListener(
            l -> this.updateStripperScope(
                "force", "add", url));
        menuItemList.add(addToBlacklistItem);
      }

      return menuItemList;
    }

    return null;
  }

}
