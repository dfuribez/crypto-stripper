import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;

import java.util.HashMap;

public class Extension implements BurpExtension {

  public boolean forceInterceptInScope = false;

  @Override
  public void initialize(MontoyaApi api) {

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    PersistedList<String> stripperScope = scope.get("scope");
    PersistedList<String> stripperBlackList = scope.get("blacklist");
    PersistedList<String> stripperForceIntercept = scope.get("force");

    api.extension().setName("Crypto Stripper");

    MainTab tab = new MainTab(
        api,
        stripperScope,
        stripperBlackList,
        stripperForceIntercept
    );

    tab.setScopeList("scope", stripperScope);
    tab.setScopeList("blacklist", stripperBlackList);
    tab.setScopeList("force", stripperForceIntercept);

    api.userInterface().registerSuiteTab("Stripper", tab.panel1);
    api.userInterface()
        .registerContextMenuItemsProvider(new MyContextMenus(
            api,
            tab,
            stripperScope,
            stripperBlackList,
            stripperForceIntercept
        ));
    api.userInterface().registerHttpRequestEditorProvider(
        new MyHttpRequestEditorProvider(
            api,
            stripperScope,
            stripperBlackList,
            stripperForceIntercept
        ));

    api.http().registerHttpHandler(new MyHttpHandler(
        api,
        tab,
        stripperScope
    ));

    api.proxy().registerRequestHandler(new ProxyHttpRequestHandler(
        api,
        tab,
        stripperScope,
        stripperBlackList,
        stripperForceIntercept
    ));

  }
}
