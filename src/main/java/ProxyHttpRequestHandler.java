import static burp.api.montoya.http.message.HttpHeader.httpHeader;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;

import java.util.HashMap;

import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;


class ProxyHttpRequestHandler implements ProxyRequestHandler {
  Logging logger;
  MontoyaApi api;
  public MainTab mainTab;

  public ProxyHttpRequestHandler(MontoyaApi api, MainTab tab) {
    this.api = api;
    this.logger = api.logging();
    this.mainTab = tab;
  }

  @Override
  public ProxyRequestReceivedAction handleRequestReceived(
    InterceptedRequest interceptedRequest
  ) {

    String url = Utils.removeQueryFromUrl(interceptedRequest.url());
    Annotations annotations = interceptedRequest.annotations();

    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    if (interceptedRequest.hasHeader(Constants.FIREPROXY_HEADER)) {
      String[] value = interceptedRequest.headerValue(
          Constants.FIREPROXY_HEADER).split(",", 2);

      if (value.length == 2) {
        annotations = annotations
            .withHighlightColor(HighlightColor.valueOf(value[0].toUpperCase()))
            .withNotes(value[1]);
      }
    }

    if (scope.get("blacklist").contains(url)) {
      return ProxyRequestReceivedAction.doNotIntercept(interceptedRequest);
    }

    if (this.mainTab.requestCheckBox.isSelected() &&
        scope.get("scope").contains(url)
    ) {
      HashMap<String, String> preparedForExecute =
          Utils.prepareRequestForExecutor(
              interceptedRequest, interceptedRequest.messageId());
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

      if (this.mainTab.forceInterceptInScopeCheckbox.isSelected()) {
        return ProxyRequestReceivedAction.intercept(
            decryptedRequest,
            annotations);
      }

      return ProxyRequestReceivedAction.continueWith(
          decryptedRequest,
          annotations);
    }

    if (scope.get("force").contains(url)) {
      return ProxyRequestReceivedAction.intercept(
          interceptedRequest,
          annotations);
    }

    return ProxyRequestReceivedAction.continueWith(
        interceptedRequest,
        annotations);
  }


  @Override
  public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {

    return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
  }
}
 