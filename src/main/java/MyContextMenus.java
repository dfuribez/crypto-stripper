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

public class MyContextMenus  implements ContextMenuItemsProvider {
  private final MontoyaApi api;
  public PersistedList<String> stripperScope;
  public PersistedList<String> blackList;
  public PersistedList<String> forceInterceptList;
  private MainTab mainTab;

  public MyContextMenus(
      MontoyaApi api,
      MainTab tab,
      PersistedList<String> stripperScope,
      PersistedList<String> stripperBlackList,
      PersistedList<String> stripperForceIntercept
  ) {
    this.api = api;
    this.mainTab = tab;
    this.stripperScope = stripperScope;
    this.blackList = stripperBlackList;
    this.forceInterceptList = stripperForceIntercept;
  }

  public void updateStripperScope(
      String source,
      String action,
      String url
  ) {

    PersistedList<String> target;
    String key;

    switch (source) {
      case "blacklist":
        target = this.blackList;
        key = Constants.STRIPPER_BLACK_LIST_KEY;
        break;
      case "force":
        target = this.forceInterceptList;
        key = Constants.STRIPPER_FORCE_INTERCEPT;
        break;
      case "scope":
        target = this.stripperScope;
        key = Constants.STRIPPER_SCOPE_KEY;
        break;
      default:
        return;
    }

    if ("add".equals(action)) {
      target.add(url);
    } else {
      target.remove(url);
    }

    this.api.persistence()
        .extensionData()
        .setStringList(key, target);

    this.mainTab.setForceIntercept(this.forceInterceptList);
    this.mainTab.setBlackList(this.blackList);
    this.mainTab.setScopeList(this.stripperScope);
  }

  public void decryptRequest(
      MessageEditorHttpRequestResponse requestResponse
  ){
    HttpRequest request = requestResponse.requestResponse().request();

    // TODO: calculate own messageID since the api does not provide it for this object
    HashMap<String, String> preparedToExecute =
        Utils.prepareRequestForExecutor(request, 0);

    ExecutorResponse executorResponse = Executor.execute(
      this.api,
      "decrypt",
      "request",
      preparedToExecute
    );

    requestResponse.setRequest(Utils.executorToHttp(request, executorResponse));

  }


  @Override
  public List<Component> provideMenuItems(
      ContextMenuEvent event
  ) {

    List<Component> menuItemList = new ArrayList<>();
    if (event.messageEditorRequestResponse().isEmpty()) {
      return null;
    }

    MessageEditorHttpRequestResponse requestResponse = event
        .messageEditorRequestResponse()
        .get();

    String url = Utils.removeQueryFromUrl(
        requestResponse
            .requestResponse()
            .request()
            .url()
    );

    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER) &&
        event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST)
    ) {

      if (this.stripperScope.contains(url)) {
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
      if (this.stripperScope.contains(url)) {
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
      if (this.blackList.contains(url)) {
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

      if (this.forceInterceptList.contains(url)) {
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
