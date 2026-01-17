
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.persistence.PersistedList;
import models.ExecutorOutput;

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

    Annotations annotations = requestToBeSent.annotations();

    if (requestToBeSent.method().equalsIgnoreCase("options")) {
      return continueWith(modifiedRequest, annotations);
    }

    String url = Utils.removeQueryFromUrl(requestToBeSent.url());
    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    if (this.mainTab.requestCheckBox.isSelected()
        && Utils.isUrlInScope(url, scope.get("scope"))
    ) {
      String toolName = requestToBeSent.toolSource().toolType().toolName().toLowerCase();
      HashMap<String, String> preparedToExecute =
          Utils.prepareRequestForExecutor(modifiedRequest, requestToBeSent.messageId(), toolName);
      ExecutorOutput executorOutput = Executor.execute(
          api, "encrypt", "request", preparedToExecute);

      if (executorOutput.issue != null) {
        Utils.setIssue(api, executorOutput.issue, url, requestToBeSent, HttpResponse.httpResponse());
      }

      if (executorOutput.annotation != null) {
        annotations = Utils.setAnnotation(
            annotations.notes(),
            executorOutput.annotation.get("color"),
            executorOutput.annotation.get("note")
        );
      }

      return continueWith(
          Utils.executorToHttpRequest(modifiedRequest, executorOutput)
              .withRemovedHeader(Constants.STRIPPER_HEADER),
          annotations
      );
    }

    return continueWith(modifiedRequest, annotations);
  }

  @Override
  public ResponseReceivedAction handleHttpResponseReceived(
      HttpResponseReceived responseReceived
  ) {
    String url = Utils.removeQueryFromUrl(
        responseReceived.initiatingRequest().url());
    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    HttpResponse response = responseReceived
        .withStatusCode(responseReceived.statusCode());

    Annotations annotations = responseReceived.annotations();

    boolean isUrlInScope = Utils.isUrlInScope(url, scope.get("scope"));

    if (this.mainTab.responseCheckBox.isSelected() && isUrlInScope) {

      String source = responseReceived.toolSource().toolType().toolName().toLowerCase();

      HashMap<String, String> preparedToExecute =
          Utils.prepareResponseForExecutor(responseReceived, url, responseReceived.messageId(), source);

      ExecutorOutput executorOutput = Executor.execute(
          api, "decrypt", "response", preparedToExecute);

      HttpResponse decryptedResponse =
          Utils.executorToHttpResponse(responseReceived, executorOutput);

      if (executorOutput.issue != null) {
        Utils.setIssue(api, executorOutput.issue, url, responseReceived.initiatingRequest(), responseReceived);
      }

      if (executorOutput.annotation != null) {
        annotations = Utils.setAnnotation(
            annotations.notes(),
            executorOutput.annotation.get("color"),
            executorOutput.annotation.get("note")
        );
      }

      if (responseReceived.toolSource().isFromTool(ToolType.PROXY)) {
          return continueWith(decryptedResponse, annotations);
      }

      return continueWith(decryptedResponse, annotations);
    }

    if (isUrlInScope) {
      response = response
          .withAddedHeader(Constants.STRIPPER_HEADER, Constants.X_STRIPPER_RESPONSE_NOT_SELECTED);
    }

    return continueWith(response, annotations);
  }
}
