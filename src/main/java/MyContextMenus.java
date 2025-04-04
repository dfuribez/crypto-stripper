import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import jdk.jshell.execution.Util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static burp.api.montoya.persistence.PersistedList.persistedStringList;

public class MyContextMenus  implements ContextMenuItemsProvider {
  private final MontoyaApi api;
  public PersistedList<String> stripperScope;
  public PersistedList<String> stripperBlackList;
  public PersistedList<String> stripperForceIntercept;

  public MyContextMenus(
      MontoyaApi api,
      PersistedList<String> stripperScope,
      PersistedList<String> stripperBlackList,
      PersistedList<String> stripperForceIntercept
  ) {
    this.api = api;
    this.stripperScope = stripperScope;
    this.stripperBlackList = stripperBlackList;
    this.stripperForceIntercept = stripperForceIntercept;
  }

  public void updateStripperScope(String action, String url) {

    if ("add".equals(action)) {
      this.stripperScope.add(url);
    } else {
      this.stripperScope.remove(url);
    }

    this.api.persistence()
        .extensionData()
        .setStringList(
            Constants.STRIPPER_SCOPE_KEY,
            this.stripperScope
        );
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
    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER) &&
        event.isFrom(InvocationType.MESSAGE_EDITOR_REQUEST)
    ) {

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

      List<Component> menuItemList = new ArrayList<>();

      if (this.stripperScope.contains(url)) {
        JMenuItem item = new JMenuItem("Decrypt");
        item.addActionListener(l -> this.decryptRequest(requestResponse));

        JMenuItem removeScopeItem = new JMenuItem("Remove from scope");
        removeScopeItem.addActionListener(
            l -> this.updateStripperScope("remove", url));

        menuItemList.add(item);
        menuItemList.add(removeScopeItem);
      } else {
        JMenuItem item = new JMenuItem("Add url to scope");
        item.addActionListener(
            l -> this.updateStripperScope("add", url));
        menuItemList.add(item);
      }

      return menuItemList;
    }

    return null;
  }

}
