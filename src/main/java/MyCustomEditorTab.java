import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;

import java.awt.*;

public class MyCustomEditorTab implements ExtensionProvidedHttpRequestEditor {
  MontoyaApi api;
  EditorCreationContext creationContext;
  EditorTab editorTab;

  private PersistedList scope;
  private PersistedList blackList;
  private PersistedList forceIntercept;

  private HttpRequestResponse currentRequest;

  public MyCustomEditorTab(
      MontoyaApi api,
      EditorCreationContext editorCreationContext,
      PersistedList scope,
      PersistedList blackList,
      PersistedList forceIntercept
  ) {
    this.api = api;
    this.creationContext = editorCreationContext;
    this.editorTab = new EditorTab(api);
    this.scope = scope;
    this.blackList = blackList;
    this.forceIntercept = forceIntercept;
  }

  @Override
  public HttpRequest getRequest() {
    return this.currentRequest.request();
  }

  @Override
  public void setRequestResponse(HttpRequestResponse requestResponse) {
    this.currentRequest = requestResponse;
  }

  @Override
  public boolean isEnabledFor(HttpRequestResponse requestResponse) {
    String url =
        Utils.removeQueryFromUrl(requestResponse.request().url());
    this.currentRequest = requestResponse;
    if (this.scope.contains(url)) {
      this.editorTab.setCommand(requestResponse.request().toByteArray());
      return true;
    }

    return false;
  }

  @Override
  public String caption() {
    return "Stripper";
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
