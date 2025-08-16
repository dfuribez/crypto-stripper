import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import com.google.gson.*;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class MyContextMenus  implements ContextMenuItemsProvider {
  private final MontoyaApi montoyaApi;
  private MainTab mainTab;
  InsertDialog insertDialog;

  public MyContextMenus(MontoyaApi api, MainTab tab) {
    this.montoyaApi = api;
    this.mainTab = tab;
    insertDialog = new InsertDialog(montoyaApi);
  }

  public void updateStripperScope(String source, String action, String url) {
    PersistedList<String> target;
    String key;

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(montoyaApi.persistence().extensionData());

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

    montoyaApi.persistence().extensionData().setStringList(key, target);
    mainTab.loadCurrentSettings();
  }

  public void decryptRequest(MessageEditorHttpRequestResponse requestResponse, String source){
    HttpRequest request = requestResponse.requestResponse().request();

    HashMap<String, String> preparedToExecute =
        Utils.prepareRequestForExecutor(request, -1, source);

    ExecutorOutput executorResponse =
        Executor.execute(montoyaApi, "decrypt", "request", preparedToExecute);

    requestResponse.setRequest(Utils.executorToHttpRequest(request, executorResponse));
  }


  @Override
  public List<Component> provideMenuItems(ContextMenuEvent event) {
    List<Component> menuItemList = new ArrayList<>();

    HttpRequestResponse requestResponse = null;
    MessageEditorHttpRequestResponse editorHttpRequestResponse;

    if (event.messageEditorRequestResponse().isPresent()) {
      requestResponse = event.messageEditorRequestResponse().get().requestResponse();
      editorHttpRequestResponse = event.messageEditorRequestResponse().get();
    } else {
      editorHttpRequestResponse = null;
      requestResponse = event.selectedRequestResponses().getFirst();
    }

    String url = Utils.removeQueryFromUrl(requestResponse.request().url());

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(this.montoyaApi.persistence().extensionData());

    String source = event.toolType().toolName().toLowerCase();

    JMenuItem insertPayload = null;
    JMenuItem decryptMenu = null;
    JMenuItem removeScopeItem = null;
    JMenuItem addUrlToScopeMenu = null;
    JMenuItem removeFromBlacklistItem = null;
    JMenuItem dontForceInterceptionMenu = null;
    JMenuItem addToBlacklistItem = null;
    JMenuItem addToForceInterceptMenu = null;
    JMenuItem addToPassThrough = null;


    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER) && editorHttpRequestResponse != null) {
      if (Utils.isUrlInScope(url, scope.get("scope"))) {
        decryptMenu = new JMenuItem("Decrypt");
        decryptMenu.addActionListener(
            l -> this.decryptRequest(editorHttpRequestResponse, source));
      }

      insertPayload = new JMenuItem("Insert payload");
      insertPayload.addActionListener(l -> {
        int cursorPosition = editorHttpRequestResponse.caretPosition();
        byte[] content = editorHttpRequestResponse.requestResponse().request().toByteArray().getBytes();

        insertDialog.pack();
        insertDialog.setVisible(true);

        byte[] toInsert = InsertDialog.selectedText;

        if (toInsert == null) { return; }

        if (insertDialog.base64RadioButton.isSelected()) {
          toInsert = montoyaApi.utilities().base64Utils().encodeToString(
              ByteArray.byteArray(toInsert)).getBytes();
        }

        if (insertDialog.URLEncodeRadioButton.isSelected()) {
          toInsert = URLEncoder.encode(
              new String(toInsert, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        }

        byte[] s = Arrays.copyOfRange(content, 0, cursorPosition);
        byte[] f = Arrays.copyOfRange(content, cursorPosition, content.length);

        byte[] nRequest = new byte[s.length + toInsert.length + f.length];

        System.arraycopy(s, 0, nRequest, 0, s.length);
        System.arraycopy(toInsert, 0, nRequest, s.length, toInsert.length);
        System.arraycopy(f, 0, nRequest, s.length + toInsert.length, f.length);

        editorHttpRequestResponse.setRequest(
            HttpRequest.httpRequest(ByteArray.byteArray(nRequest)));
      });
    }

    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
      if (Utils.isUrlInScope(url, scope.get("scope"))) {
        removeScopeItem = new JMenuItem("Remove from scope");
        removeScopeItem.addActionListener(
            l -> this.updateStripperScope("scope", "remove", url));
      } else {
        addUrlToScopeMenu = new JMenuItem("Add url to scope");
        addUrlToScopeMenu.addActionListener(
            l -> this.updateStripperScope("scope", "add", url));
      }

      if (Utils.isUrlInScope(url, scope.get("blacklist"))) {
        removeFromBlacklistItem = new JMenuItem("Remove endpoint from blacklist");
        removeFromBlacklistItem.addActionListener(
            l -> this.updateStripperScope("blacklist", "remove", url));
      } else {
        addToBlacklistItem = new JMenuItem("Add endpoint to blacklist");
        addToBlacklistItem.addActionListener(
            l -> this.updateStripperScope("blacklist", "add", url));
      }

      if (Utils.isUrlInScope(url, scope.get("force"))) {
        dontForceInterceptionMenu = new JMenuItem("Do not force interception");
        dontForceInterceptionMenu.addActionListener(
            l -> this.updateStripperScope("force", "remove", url));
      } else {
        addToForceInterceptMenu = new JMenuItem("Force interception this endpoint");
        addToForceInterceptMenu.addActionListener(
            l -> this.updateStripperScope("force", "add", url));
      }

      addToPassThrough = new JMenuItem("Add host to Burp's pass through");
      HttpRequestResponse finalRequestResponse = requestResponse;
      addToPassThrough.addActionListener(l -> {
        String host = finalRequestResponse.request().httpService().host()
            .replace(".", "\\\\.");
        int port = finalRequestResponse.request().httpService().port();
        String pass = String.format(Constants.PASS_THROUGH, host, port);

        String current = montoyaApi.burpSuite().exportProjectOptionsAsJson();

        JsonObject root = JsonParser.parseString(current).getAsJsonObject();

        JsonArray rules = root
            .getAsJsonObject("proxy")
            .getAsJsonObject("ssl_pass_through")
            .getAsJsonArray("rules");

        rules.add(JsonParser.parseString(pass).getAsJsonObject());

        String test = new Gson().toJson(root);
        montoyaApi.burpSuite().importProjectOptionsFromJson(test);
      });
    }


    menuItemList.add(decryptMenu);

    menuItemList.add(addUrlToScopeMenu);
    menuItemList.add(removeScopeItem);

    menuItemList.add(addToBlacklistItem);
    menuItemList.add(removeFromBlacklistItem);

    menuItemList.add(addToForceInterceptMenu);
    menuItemList.add(dontForceInterceptionMenu);

    menuItemList.add(addToPassThrough);
    menuItemList.add(insertPayload);

    return menuItemList;
  }
}
