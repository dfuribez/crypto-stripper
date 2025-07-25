import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;

import java.awt.*;
import java.util.HashMap;

public class MyCustomResponseEditor implements ExtensionProvidedHttpResponseEditor {
  MontoyaApi api;
  EditorCreationContext creationContext;
  EditorTab editorTab;

  HttpRequestResponse currentResponse;

  public MyCustomResponseEditor(MontoyaApi api, EditorCreationContext editorCreationContext) {
    this.api = api;
    this.creationContext = editorCreationContext;
    String tool = editorCreationContext.toolSource().toolType().toolName().toLowerCase();
    this.editorTab = new EditorTab(api, false, tool);
  }

  @Override
  public HttpResponse getResponse() {
    return this.currentResponse.response();
  }

  @Override
  public void setRequestResponse(HttpRequestResponse requestResponse) {
    this.currentResponse = requestResponse;
    this.editorTab.setRequestResponse(requestResponse);
    this.editorTab.setContent(requestResponse.response());
  }

  @Override
  public boolean isEnabledFor(HttpRequestResponse requestResponse) {

    if (requestResponse.request() == null) {
      return false;
    }

    String url =
        Utils.removeQueryFromUrl(requestResponse.request().url());
    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());
    
    this.currentResponse = requestResponse;
    this.editorTab.setRequestResponse(requestResponse);
    return Utils.isUrlInScope(url, scope.get("scope"));
  }

  @Override
  public String caption() {
    return "Stripper preview";
  }

  @Override
  public Component uiComponent() {
    return this.editorTab.panel1;
  }

  @Override
  public Selection selectedData() {
    return null;
  }

  @Override
  public boolean isModified() {
    return false;
  }
}
