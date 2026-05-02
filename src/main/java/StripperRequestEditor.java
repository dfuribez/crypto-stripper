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

public class StripperRequestEditor implements ExtensionProvidedHttpRequestEditor {
  MontoyaApi montoyaApi;
  EditorCreationContext creationContext;
  PreviewTabGUI previewTabGUI;


  private HttpRequestResponse currentRequest;

  public StripperRequestEditor(MontoyaApi montoyaApi, EditorCreationContext editorCreationContext) {
    this.montoyaApi = montoyaApi;
    this.creationContext = editorCreationContext;

    String source = editorCreationContext.toolSource().toolType().toolName().toLowerCase();

    this.previewTabGUI = new PreviewTabGUI(montoyaApi, true, source);
  }

  @Override
  public HttpRequest getRequest() {
    return this.currentRequest.request();
  }

  @Override
  public void setRequestResponse(HttpRequestResponse requestResponse) {
    currentRequest = requestResponse;
    previewTabGUI.setRequestResponse(requestResponse);
    previewTabGUI.setContent(requestResponse.request());
  }

  @Override
  public boolean isEnabledFor(HttpRequestResponse requestResponse) {
    try {
      String url =
          Utils.removeQueryFromUrl(requestResponse.request().url());

      HashMap<String, PersistedList<String>> scope =
          Utils.loadScope(montoyaApi.persistence().extensionData());
      currentRequest = requestResponse;
      previewTabGUI.setRequestResponse(requestResponse);

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
    return previewTabGUI.mainPanel;
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
