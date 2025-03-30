import static burp.api.montoya.http.message.HttpHeader.httpHeader;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;

import java.util.ArrayList;

import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;


class ProxyHttpRequestHandler implements ProxyRequestHandler {
  PersistedList<String> stripperScope;
  Logging logger;
  public MainTab tab;

  public ProxyHttpRequestHandler(
      MontoyaApi api,
      PersistedList<String> stripperScope,
      MainTab tab
  ) {
    this.stripperScope = stripperScope;
    this.logger = api.logging();
    this.tab = tab;
  }

  @Override
  public ProxyRequestReceivedAction handleRequestReceived(
    InterceptedRequest interceptedRequest) {

    HttpRequest modified = interceptedRequest.withAddedHeader(httpHeader("test","yes"));


    //.continueWith follow current interception rules
    //.intercept intercepts the request no matter the interception rule

    if (this.tab.interceptionScopeCheckBox.isSelected() &&
      this.stripperScope.contains(interceptedRequest.url())
    ) {
      return ProxyRequestReceivedAction.intercept(modified);
    } else {
      return ProxyRequestReceivedAction.continueWith(modified);
    }
  }


  @Override
  public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {

    return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
  }
}
 