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

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static burp.api.montoya.persistence.PersistedList.persistedStringList;

public class MyContextMenus  implements ContextMenuItemsProvider {
  private final MontoyaApi api;
  public PersistedList<String> stripperScope;

  public MyContextMenus(
      MontoyaApi api,
      PersistedList<String> stripperScope
  ) {
    this.api = api;
    this.stripperScope = stripperScope;
  }

  public void addUrlToScope(String url) {
    this.stripperScope.add(url);

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

    requestResponse.setRequest(request
        .withAddedHeader(
            HttpHeader.httpHeader(
                "X-Stripper",
                "true"
            )
        ));
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

      String url = requestResponse
          .requestResponse()
          .request()
          .url();

      List<Component> menuItemList = new ArrayList<>();

      if (this.stripperScope.contains(url)) {
        JMenuItem item = new JMenuItem("Decrypt");
        item.addActionListener(l -> this.decryptRequest(requestResponse));
        menuItemList.add(item);
      } else {
        JMenuItem item = new JMenuItem("Add url to scope");
        item.addActionListener(l -> this.addUrlToScope(url));
        menuItemList.add(item);
      }

      return menuItemList;
    }

    return null;
  }

}
