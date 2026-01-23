import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import com.google.gson.*;
import models.ExecutorOutput;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    if (requestResponse == null) {
      return;
    }

    HttpRequest request = requestResponse.requestResponse().request();

    HashMap<String, String> preparedToExecute =
        Utils.prepareRequestForExecutor(request, -1, source);

    ExecutorOutput executorResponse =
        Executor.execute(montoyaApi, "decrypt", "request", preparedToExecute);

    try {
      requestResponse.setRequest(Utils.executorToHttpRequest(request, executorResponse));
    } catch (Exception e) {
      montoyaApi.logging().logToError("MyContextMenus.decryptRequest: " + e);
    }

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

    JMenuItem insertPayloadMenu = null;
    JMenuItem decryptMenu = null;
    JMenuItem stripperScopeMenu = null;
    JMenuItem stripperBlackListMenu = null;
    JMenuItem stripperForceMenu = null;
    JMenuItem addToPassThroughMenu = null;
    JMenuItem burpScopeMenu = null;


    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER) && editorHttpRequestResponse != null) {
      if (Utils.isUrlInScope(url, scope.get("scope"))
          && !event.isFrom(InvocationType.MESSAGE_VIEWER_REQUEST)
          && !event.isFrom(InvocationType.MESSAGE_VIEWER_RESPONSE)
      ) {
        decryptMenu = new JMenuItem("Decrypt");
        decryptMenu.addActionListener(
            l -> this.decryptRequest(editorHttpRequestResponse, source));
      }

      insertPayloadMenu = new JMenuItem("Insert payload");
      insertPayloadMenu.addActionListener(l -> {
        int cursorPosition = editorHttpRequestResponse.caretPosition();
        byte[] content = editorHttpRequestResponse
            .requestResponse().request().toByteArray().getBytes();

        HttpRequest request =
            editorHttpRequestResponse.requestResponse().request();

        insertDialog.setParameters(request.parameters());
        insertDialog.pack();
        insertDialog.setVisible(true);

        String selectedComboParameter = insertDialog.getSelectedParameter();
        String[] split = selectedComboParameter.split(" - ", 2);

        String selectedParameter = split[1];
        byte[] toInsert = InsertDialog.selectedText;

        if (toInsert == null) { return; }

        if (insertDialog.base64RadioButton.isSelected()) {
          toInsert = montoyaApi.utilities().base64Utils().encodeToString(
              ByteArray.byteArray(toInsert)
          ).getBytes();
        }

        if (insertDialog.URLEncodeRadioButton.isSelected()) {
          toInsert = URLEncoder.encode(
              new String(toInsert, StandardCharsets.UTF_8),
              StandardCharsets.UTF_8
          ).getBytes(StandardCharsets.UTF_8);
        }

        if (!selectedParameter.equals("SELECTION POINT")) {
          HttpParameterType selectedParameterType = HttpParameterType.valueOf(split[0]);
          String value = ByteArray.byteArray(toInsert).toString();

          HttpParameter parameter = HttpParameter.parameter(
              selectedParameter,
              value,
              selectedParameterType
          );

          editorHttpRequestResponse.setRequest(request.withParameter(parameter));
          return;
        }

        byte[] s = Arrays.copyOfRange(content, 0, cursorPosition);
        byte[] f = Arrays.copyOfRange(content, cursorPosition, content.length);

        byte[] nRequest = new byte[s.length + toInsert.length + f.length];

        System.arraycopy(s, 0, nRequest, 0, s.length);
        System.arraycopy(toInsert, 0, nRequest, s.length, toInsert.length);
        System.arraycopy(f, 0, nRequest, s.length + toInsert.length, f.length);

        editorHttpRequestResponse.setRequest(
            HttpRequest.httpRequest(ByteArray.byteArray(nRequest))
        );
      });
    }

    if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
      if (Utils.isUrlInScope(url, scope.get("scope"))) {
        stripperScopeMenu = new JMenuItem("Remove from scope");
        stripperScopeMenu.addActionListener(
            l -> this.updateStripperScope("scope", "remove", url));
      } else {
        stripperScopeMenu = new JMenuItem("Add url to scope");
        stripperScopeMenu.addActionListener(
            l -> {
              this.updateStripperScope("scope", "add", url);
              this.decryptRequest(editorHttpRequestResponse, source);
            }
        );
      }

      if (Utils.isUrlInScope(url, scope.get("blacklist"))) {
        stripperBlackListMenu = new JMenuItem("Remove endpoint from blacklist");
        stripperBlackListMenu.addActionListener(
            l -> this.updateStripperScope("blacklist", "remove", url)
        );
      } else {
        stripperBlackListMenu = new JMenuItem("Add endpoint to blacklist");
        stripperBlackListMenu.addActionListener(
            l -> this.updateStripperScope("blacklist", "add", url)
        );
      }

      if (Utils.isUrlInScope(url, scope.get("force"))) {
        stripperForceMenu = new JMenuItem("Do not force interception");
        stripperForceMenu.addActionListener(
            l -> this.updateStripperScope("force", "remove", url)
        );
      } else {
        stripperForceMenu = new JMenuItem("Force interception this endpoint");
        stripperForceMenu.addActionListener(
            l -> this.updateStripperScope("force", "add", url)
        );
      }

      if (montoyaApi.scope().isInScope(url)) {
        burpScopeMenu = new JMenuItem("Exclude URL from Burp's scope");
        burpScopeMenu.addActionListener(
            l -> montoyaApi.scope().excludeFromScope(Utils.removePathFromURL(url))
        );
      } else {
        burpScopeMenu = new JMenuItem("Include URL to Burp's scope");
        burpScopeMenu.addActionListener(
            l -> montoyaApi.scope().includeInScope(Utils.removePathFromURL(url))
        );
      }

      addToPassThroughMenu = new JMenuItem("Add host to Burp's pass through");
      HttpRequestResponse finalRequestResponse = requestResponse;
      addToPassThroughMenu.addActionListener(l -> {
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

    menuItemList.add(new JSeparator());
    menuItemList.add(stripperScopeMenu);
    menuItemList.add(stripperBlackListMenu);
    menuItemList.add(stripperForceMenu);

    menuItemList.add(new JSeparator());
    menuItemList.add(addToPassThroughMenu);
    menuItemList.add(burpScopeMenu);

    menuItemList.add(new JSeparator());
    menuItemList.add(insertPayloadMenu);

    return menuItemList;
  }
}
