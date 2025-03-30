import java.util.ArrayList;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;

public class Extension implements BurpExtension {

  public boolean forceInterceptInScope = false;

  @Override
  public void initialize(MontoyaApi api) {

    PersistedList<String> stripperScope = api.persistence().extensionData().getStringList(Constants.STRIPPER_SCOPE_KEY);

    if (stripperScope == null) {
      stripperScope = PersistedList.persistedStringList();
    }

    stripperScope.add("asasd");
    stripperScope.add("213123123123123");
    stripperScope.add("as564564646456456asd");
    stripperScope.add("asaytryrtyrtyrtysd");
    stripperScope.add("asartyrtyrtysd");
    stripperScope.add("asassdfd");
    stripperScope.add("213123123123123sdf");
    stripperScope.add("as564564646456456asdsdf");
    stripperScope.add("asaytryrtyrtyrtysdsfd");
    stripperScope.add("asasfrtyrtyrtysd");
    stripperScope.add("asasd");
    stripperScope.add("213123123123123");
    stripperScope.add("as564564646456456asd");
    stripperScope.add("asaytryrtyrtyrtysd");
    stripperScope.add("asartyrtyrtysd");

    for (String i : stripperScope) {
      api.logging().logToOutput(i);
    }
    ArrayList<String> stripperBlackList = new ArrayList<String>();

    api.extension().setName("Crypto Stripper");

    MainTab tab = new MainTab();

    tab.setScopeList(stripperScope);

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
