import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.proxy.http.InterceptedResponse;
import burp.api.montoya.proxy.http.ProxyResponseHandler;
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction;
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction;
import models.ExecutorOutput;

import java.util.HashMap;

class ProxyHttpResponseHandler implements ProxyResponseHandler {
  MontoyaApi api;
  MainTabGUI mainTab;

  public ProxyHttpResponseHandler(MontoyaApi api, MainTabGUI tab) {
    this.api = api;
    this.mainTab = tab;
  }

  @Override
  public ProxyResponseReceivedAction handleResponseReceived(
      InterceptedResponse interceptedResponse
  ) {
    String url = Utils.removeQueryFromUrl(interceptedResponse.initiatingRequest().url());
    HashMap<String, PersistedList<String>> scope =
        Utils.loadScope(api.persistence().extensionData());

    HttpResponse response = interceptedResponse
        .withStatusCode(interceptedResponse.statusCode());

    Annotations annotations = interceptedResponse.annotations();

    boolean isUrlInScope = Utils.isUrlInScope(url, scope.get("scope"));

    if (this.mainTab.responseCheckBox.isSelected() && isUrlInScope) {
      HashMap<String, String> preparedToExecute = Utils.prepareResponseForExecutor(
          interceptedResponse,
          url,
          interceptedResponse.messageId(),
          "proxy"
      );

      ExecutorOutput executorOutput = Executor.execute(
          api, "decrypt", "response", preparedToExecute);

      HttpResponse decryptedResponse =
          Utils.executorToHttpResponse(interceptedResponse, executorOutput);

      if (executorOutput.issue != null) {
        Utils.setIssue(
            api,
            executorOutput.issue,
            url,
            interceptedResponse.initiatingRequest(),
            interceptedResponse
        );
      }

      if (executorOutput.annotation != null) {
        annotations = Utils.setAnnotation(
            annotations.notes(),
            executorOutput.annotation.get("color"),
            executorOutput.annotation.get("note")
        );
      }

      return ProxyResponseReceivedAction.continueWith(decryptedResponse, annotations);
    }

    if (isUrlInScope) {
      response = response
          .withAddedHeader(K.HEADER.STRIPPER, K.Error.RESPONSE_NOT_SELECTED);
    }

    return ProxyResponseReceivedAction.continueWith(response, annotations);
  }

  @Override
  public ProxyResponseToBeSentAction handleResponseToBeSent(
      InterceptedResponse interceptedResponse
  ) {
    return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
  }
}
