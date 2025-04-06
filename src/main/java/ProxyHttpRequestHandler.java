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
  PersistedList<String> blackList;
  PersistedList<String> forceIntercept;

  Logging logger;
  MontoyaApi api;
  public MainTab mainTab;

  public ProxyHttpRequestHandler(
      MontoyaApi api,
      MainTab tab,
      PersistedList<String> stripperScope,
      PersistedList<String> blackList,
      PersistedList<String> forceIntercept
  ) {
    this.api = api;
    this.stripperScope = stripperScope;
    this.blackList = blackList;
    this.forceIntercept = forceIntercept;
    this.logger = api.logging();
    this.mainTab = tab;
  }

  @Override
  public ProxyRequestReceivedAction handleRequestReceived(
    InterceptedRequest interceptedRequest
  ) {

    String url = Utils.removeQueryFromUrl(interceptedRequest.url());

    if (this.blackList.contains(url)) {
      ProxyRequestReceivedAction.doNotIntercept(interceptedRequest);
    }

    if (this.mainTab.requestCheckBox.isSelected() &&
      this.stripperScope.contains(url)
    ) {
      HashMap<String, String> preparedForExecute =
          Utils.prepareRequestForExecutor(interceptedRequest, interceptedRequest.messageId());
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

    if (this.forceIntercept.contains(url)) {
      return ProxyRequestReceivedAction.intercept(interceptedRequest);
    }

    return ProxyRequestReceivedAction.continueWith(interceptedRequest);
  }


  @Override
  public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {

    return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
  }
}
 