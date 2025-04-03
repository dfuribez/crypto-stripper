import static burp.api.montoya.http.message.HttpHeader.httpHeader;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;

import java.util.HashMap;

import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;


class ProxyHttpRequestHandler implements ProxyRequestHandler {
  PersistedList<String> stripperScope;
  Logging logger;
  MontoyaApi api;
  public MainTab mainTab;

  public ProxyHttpRequestHandler(
      MontoyaApi api,
      PersistedList<String> stripperScope,
      MainTab tab
  ) {
    this.api = api;
    this.stripperScope = stripperScope;
    this.logger = api.logging();
    this.mainTab = tab;
  }

  @Override
  public ProxyRequestReceivedAction handleRequestReceived(
    InterceptedRequest interceptedRequest
  ) {

    String url = Utils.removeQueryFromUrl(interceptedRequest.url());


    //.continueWith follow current interception rules
    //.intercept intercepts the request no matter the interception rule

    if (this.mainTab.requestCheckBox.isSelected() &&
      this.stripperScope.contains(url)
    ) {
      HashMap<String, String> preparedForExecute = Utils.prepareForExecutor(interceptedRequest);
      ExecutorResponse executorResponse = Executor.execute(
          this.mainTab.api,
          "decrypt",
          "request",
          preparedForExecute
      );

      HttpRequest decryptedRequest = Utils.executorToHttp(
          interceptedRequest,
          executorResponse
      );

      if (this.mainTab.forceInterceptInScope.isSelected()) {
        return ProxyRequestReceivedAction.intercept(decryptedRequest);
      }

      return ProxyRequestReceivedAction.continueWith(decryptedRequest);
    }

    return ProxyRequestReceivedAction.continueWith(interceptedRequest);
  }


  @Override
  public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {

    return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
  }
}
 