
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.persistence.PersistedList;

import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;

import java.util.HashMap;


class MyHttpHandler implements HttpHandler {

  MontoyaApi api;
  MainTab mainTab;

  public MyHttpHandler(MontoyaApi api, MainTab gui) {
    this.api = api;
    this.mainTab = gui;
  }

  @Override
  public RequestToBeSentAction handleHttpRequestToBeSent(
      HttpRequestToBeSent requestToBeSent
  ) {

    HttpRequest modifiedRequest = requestToBeSent
        .withRemovedHeader(Constants.FIREPROXY_HEADER);

    if (requestToBeSent.method().equalsIgnoreCase("options")) {
      return continueWith(modifiedRequest);
    }

    String url = Utils.removeQueryFromUrl(requestToBeSent.url());
    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    if (this.mainTab.requestCheckBox.isSelected()
        && Utils.isUrlInScope(url, scope.get("scope"))
    ) {

      HashMap<String, String> preparedToExecute =
          Utils.prepareRequestForExecutor(modifiedRequest, requestToBeSent.messageId());
      ExecutorOutput executorOutput = Executor.execute(
          api, "encrypt", "request", preparedToExecute);

      return continueWith(
          Utils.executorToHttpRequest(modifiedRequest, executorOutput)
              .withRemovedHeader(Constants.STRIPPER_HEADER));
    }

    return continueWith(modifiedRequest);
  }

  @Override
  public ResponseReceivedAction handleHttpResponseReceived(
      HttpResponseReceived responseReceived
  ) {
    String url = Utils.removeQueryFromUrl(
        responseReceived.initiatingRequest().url());
    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    if (this.mainTab.responseCheckBox.isSelected()
        && Utils.isUrlInScope(url, scope.get("scope"))
    ) {

      HashMap<String, String> preparedToExecute =
          Utils.prepareResponseForExecutor(responseReceived, url, responseReceived.messageId());

      ExecutorOutput executorOutput = Executor.execute(
          api, "decrypt", "response", preparedToExecute);

      HttpResponse response =
          Utils.executorToHttpResponse(responseReceived, executorOutput);

      if (responseReceived.toolSource().isFromTool(ToolType.PROXY)) {
        if (executorOutput.getReplaceResponse()) {
          return continueWith(response);
        } else {
          return continueWith(responseReceived);
        }
      }

      return continueWith(response);
    }

    return continueWith(responseReceived);
  }
}
