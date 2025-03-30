import java.util.ArrayList;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;

public class Extension implements BurpExtension {

  public boolean forceInterceptInScope = false;

  @Override
  public void initialize(MontoyaApi api) {

    PersistedList<String> stripperScope = api.persistence().extensionData().getStringList(Constants.STRIPPER_SCOPE_KEY);
    PersistedList<String> stripperBlackList = api.persistence().extensionData().getStringList(Constants.STRIPPER_BLACK_LIST_KEY);
    PersistedList<String> stripperForceIntercept = api.persistence().extensionData().getStringList(Constants.STRIPPER_FORCE_INTERCEPT);

    if (stripperScope == null) {
      stripperScope = PersistedList.persistedStringList();
    }

    if (stripperBlackList == null) {
      stripperBlackList = PersistedList.persistedStringList();
    }

    if (stripperForceIntercept == null) {
      stripperForceIntercept = PersistedList.persistedStringList();
    }

    api.extension().setName("Crypto Stripper");

    MainTab tab = new MainTab();

    tab.setScopeList(stripperScope);
    tab.setBlackList(stripperBlackList);
    tab.setForceIntercept(stripperForceIntercept);

    api.userInterface().registerSuiteTab("Stripper", tab.panel1);
    api.userInterface()
        .registerContextMenuItemsProvider(new MyContextMenus(api, stripperScope));

    api.logging().logToOutput(api.project().name());

    api.http().registerHttpHandler(new MyHttpHandler(
        api,
        stripperScope
    ));  // All HTTP traffic no matter the tool

    api.proxy().registerRequestHandler(new ProxyHttpRequestHandler(
        api,
        stripperScope,
        tab
    ));

  }
}
