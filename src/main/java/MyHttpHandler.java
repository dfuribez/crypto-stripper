
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolSource;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.persistence.PersistedList;

import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;

import java.util.HashMap;


class MyHttpHandler implements HttpHandler {
  public PersistedList<String> stripperScope;

  MontoyaApi api;
  MainTab mainTab;

  public MyHttpHandler(
      MontoyaApi api,
      MainTab gui,
      PersistedList<String> stripperScope
  ) {
    this.api = api;
    this.stripperScope = stripperScope;
    this.mainTab = gui;
  }

  @Override
  public RequestToBeSentAction handleHttpRequestToBeSent(
      HttpRequestToBeSent requestToBeSent
  ) {

    HttpRequest modifiedRequest = requestToBeSent
        .withRemovedHeader(Constants.FIREPROXY_HEADER)
        .withRemovedHeader(Constants.STRIPPER_HEADER);

    if (requestToBeSent.method().equalsIgnoreCase("options")) {
      continueWith(modifiedRequest);
    }

    String url = Utils.removeQueryFromUrl(requestToBeSent.url());

    if (this.mainTab.requestCheckBox.isSelected() &&
      this.stripperScope.contains(url)
    ) {

      HashMap<String, String> preparedToExecute =
          Utils.prepareRequestForExecutor(modifiedRequest, requestToBeSent.messageId());
      ExecutorResponse executorResponse = Executor.execute(
          this.api,
          "encrypt",
          "request",
          preparedToExecute
      );

      return continueWith(
          Utils.executorToHttp(modifiedRequest, executorResponse));
    }

    return continueWith(modifiedRequest);
  }

  @Override
  public ResponseReceivedAction handleHttpResponseReceived(
      HttpResponseReceived responseReceived
  ) {
    String url = Utils.removeQueryFromUrl(
        responseReceived.initiatingRequest().url());

    if (this.mainTab.responseCheckBox.isSelected()
        && this.stripperScope.contains(url)
    ) {
      HashMap<String, String> preparedToExecute =
          Utils.prepareResponseForExecutor(responseReceived);

      ExecutorResponse executorResponse = Executor.execute(
          this.api,
          "decrypt",
          "response",
          preparedToExecute
      );

      HttpResponse response =
          Utils.executorToHttpResponse(responseReceived, executorResponse);


      if (responseReceived.toolSource().isFromTool(ToolType.PROXY)) {
        if (executorResponse.getReplaceResponse()) {
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
