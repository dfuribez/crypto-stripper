import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import burp.api.montoya.ui.editor.extension.HttpRequestEditorProvider;

public class MyHttpRequestEditorProvider implements HttpRequestEditorProvider {

  private final MontoyaApi api;
  private PersistedList scope;
  private PersistedList blackList;
  private PersistedList forceIntercept;

  MyHttpRequestEditorProvider(
      MontoyaApi api,
      PersistedList scope,
      PersistedList blackList,
      PersistedList forceIntercept
  ) {
    this.api = api;
    this.scope = scope;
    this.blackList = blackList;
    this.forceIntercept = forceIntercept;
  }

  @Override
  public ExtensionProvidedHttpRequestEditor provideHttpRequestEditor(
      EditorCreationContext creationContext
  ) {
    return new MyCustomEditorTab(
        this.api,
        creationContext,
        this.scope,
        this.blackList,
        this.forceIntercept
    );
  }

}
