
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedList;

import static burp.api.montoya.http.message.HttpHeader.httpHeader;
import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;
import static burp.api.montoya.http.message.params.HttpParameter.urlParameter;

import java.util.HashMap;


class MyHttpHandler implements HttpHandler {
  private final Logging logging;
  public PersistedList<String> stripperScope;
  public boolean forceInterceptInScope;

  MontoyaApi api;

  MainTab mainTab;

  public MyHttpHandler(
      MontoyaApi api,
      PersistedList<String> stripperScope,
      MainTab gui
  ) {
    this.api = api;
    this.logging = api.logging();
    this.stripperScope = stripperScope;
    this.mainTab = gui;
  }

  @Override
  public RequestToBeSentAction handleHttpRequestToBeSent(
      HttpRequestToBeSent requestToBeSent
  ) {
    //Annotations annotations = requestToBeSent.annotations();

    //annotations = annotations.withNotes("tesing");
    //annotations = annotations.withHighlightColor(HighlightColor.BLUE);

    String url = Utils.removeQueryFromUrl(requestToBeSent.url());

    if (this.mainTab.requestCheckBox.isSelected() &&
      this.stripperScope.contains(url)
    ) {

      HashMap<String, String> preparedToExecute =
          Utils.prepareRequestForExecutor(requestToBeSent, requestToBeSent.messageId());
      ExecutorResponse executorResponse = Executor.execute(
          this.api,
          "encrypt",
          "request",
          preparedToExecute
      );

      return continueWith(Utils.executorToHttp(requestToBeSent, executorResponse));
    }

    return continueWith(requestToBeSent);
  }

  @Override
  public ResponseReceivedAction handleHttpResponseReceived(
      HttpResponseReceived responseReceived
  ) {

    String url = Utils.removeQueryFromUrl(
        responseReceived.initiatingRequest().url());

    if (this.mainTab.responseCheckBox.isSelected()
        && this.stripperScope.contains(url)
        && responseReceived.initiatingRequest().hasHeader(Constants.STRIPPER_HEADER)
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

      return continueWith(response);
    }

    return continueWith(responseReceived);
  }
}
