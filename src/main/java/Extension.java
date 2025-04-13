import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;

import java.util.HashMap;

public class Extension implements BurpExtension {

  @Override
  public void initialize(MontoyaApi api) {

    api.extension().setName("Crypto Stripper");

    MainTab tab = new MainTab(api);

    api.userInterface().registerSuiteTab("Stripper", tab.panel1);
    api.userInterface().registerContextMenuItemsProvider(
        new MyContextMenus(api, tab));
    api.userInterface().registerHttpRequestEditorProvider(
        new MyHttpRequestEditorProvider(api));
    api.userInterface().registerHttpResponseEditorProvider(
        new MyHttpResponseEditorProvider(api));

    api.http().registerHttpHandler(new MyHttpHandler(api, tab));

    api.proxy().registerRequestHandler(
        new ProxyHttpRequestHandler(api, tab));

  }
}
