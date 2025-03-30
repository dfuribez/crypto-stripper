
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.utilities.json.JsonObjectNode;
import burp.api.montoya.utilities.json.JsonUtils;
import com.google.gson.Gson;

import static burp.api.montoya.http.message.HttpHeader.httpHeader;
import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;
import static burp.api.montoya.http.message.params.HttpParameter.urlParameter;

import java.util.ArrayList;
import java.util.List;


class MyHttpHandler implements HttpHandler {
  private final Logging logging;
  public PersistedList<String> stripperScope;
  public boolean forceInterceptInScope;


  public MyHttpHandler(MontoyaApi api,  PersistedList<String> stripperScope) {
    this.logging = api.logging();
    this.stripperScope = stripperScope;
  }

  @Override
  public RequestToBeSentAction handleHttpRequestToBeSent(
      HttpRequestToBeSent requestToBeSent
  ) {
    Annotations annotations = requestToBeSent.annotations();

    annotations = annotations.withNotes("tesing");
    annotations = annotations.withHighlightColor(HighlightColor.BLUE);

    HttpRequest modifiedRequest = requestToBeSent.withAddedParameters(urlParameter("foo", "bar"));

    String body = requestToBeSent.body().toString();
    List<HttpHeader> headers = requestToBeSent.headers();
    List<ParsedHttpParameter> getParameters = requestToBeSent.parameters(HttpParameterType.URL);

    String headersJSON = new Gson()
        .toJson(Utils.burpListToArray(headers));

    String getParamsJSON = new Gson()
        .toJson(Utils.burpListToArray(getParameters));

    return continueWith(modifiedRequest, annotations);
  }

  @Override
  public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {

    HttpResponse modifiedResponse = responseReceived.withAddedHeader(httpHeader("test", "new"));

    return continueWith(modifiedResponse);
  }
}
