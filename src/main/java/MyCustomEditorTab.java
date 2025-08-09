import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

public class MyCustomEditorTab implements ExtensionProvidedHttpRequestEditor {
  MontoyaApi montoyaApi;
  EditorCreationContext creationContext;
  EditorTab editorTab;


  private HttpRequestResponse currentRequest;

  public MyCustomEditorTab(MontoyaApi montoyaApi, EditorCreationContext editorCreationContext) {
    this.montoyaApi = montoyaApi;
    this.creationContext = editorCreationContext;

    String source = editorCreationContext.toolSource().toolType().toolName().toLowerCase();

    this.editorTab = new EditorTab(montoyaApi, true, source);
  }

  @Override
  public HttpRequest getRequest() {
    return this.currentRequest.request();
  }

  @Override
  public void setRequestResponse(HttpRequestResponse requestResponse) {
    this.currentRequest = requestResponse;
    this.editorTab.setRequestResponse(requestResponse);
    this.editorTab.setContent(requestResponse.request());
  }

  @Override
  public boolean isEnabledFor(HttpRequestResponse requestResponse) {
    try {
      String url =
          Utils.removeQueryFromUrl(requestResponse.request().url());

      HashMap<String, PersistedList<String>> scope =
          Utils.loadScope(montoyaApi.persistence().extensionData());
      this.currentRequest = requestResponse;
      this.editorTab.setRequestResponse(requestResponse);

      return Utils.isUrlInScope(url, scope.get("scope"))
          && !Objects.equals(creationContext.toolSource().toolType().toolName(), "Extensions");
    } catch (Exception e){
      return false;
    }

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
