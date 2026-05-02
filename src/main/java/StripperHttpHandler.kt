import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.ToolType
import burp.api.montoya.http.handler.*
import burp.api.montoya.http.handler.RequestToBeSentAction.continueWith
import burp.api.montoya.http.handler.ResponseReceivedAction.continueWith

class StripperHttpHandler(
  var montoyaApi: MontoyaApi,
  var stripperTab: MainTabGUI
) : HttpHandler {
  override fun handleHttpRequestToBeSent(requestToBeSent: HttpRequestToBeSent?): RequestToBeSentAction? {
    if (requestToBeSent == null) return null

    val modifiedRequest = requestToBeSent.withRemovedHeader(K.HEADER.FIREPROXY)
    val annotations = requestToBeSent.annotations()

    if (requestToBeSent.method().equals("options", ignoreCase = true)) {
      return continueWith(modifiedRequest)
    }

    val url = KUtils.Url.clean(requestToBeSent.url())
    val scope = Utils.loadScope(montoyaApi.persistence().extensionData())

    if (!(stripperTab.requestCheckBox.isSelected
      && Utils.isUrlInScope(url, scope["scope"])))
      return continueWith(modifiedRequest, annotations)

    val toolName = requestToBeSent.toolSource().toolType().toolName().lowercase()

    val editedRequest = KUtils.Request.edit(
      montoyaApi,
      modifiedRequest,
      annotations,
      requestToBeSent.messageId(),
      "encrypt",
      toolName
    )

    val newRequest = editedRequest.request.withRemovedHeader(K.HEADER.STRIPPER)
    return continueWith(newRequest, annotations)
  }

  override fun handleHttpResponseReceived(responseReceived: HttpResponseReceived?): ResponseReceivedAction? {
    if (responseReceived == null) return null
    if (responseReceived.toolSource().isFromTool(ToolType.PROXY)) {
      return continueWith(responseReceived, responseReceived.annotations())
    }

    val url = KUtils.Url.clean(responseReceived.initiatingRequest().url())
    val scope = Utils.loadScope(montoyaApi.persistence().extensionData())

    var annotations = responseReceived.annotations()

    if (!Utils.isUrlInScope(url, scope["scope"])) {
      return continueWith(responseReceived, annotations)
    }

    if (!stripperTab.responseCheckBox.isSelected) {
      return continueWith(
        responseReceived.withAddedHeader(K.HEADER.STRIPPER, K.Error.RESPONSE_NOT_SELECTED),
        annotations
      )
    }

    val source = responseReceived.toolSource().toolType().toolName().lowercase()
    val ready =
      Utils.prepareResponseForExecutor(responseReceived, url, responseReceived.messageId(), source)

    val executed = Executor.execute(montoyaApi, "decrypt", "response", ready);

    val response = Utils.executorToHttpResponse(responseReceived, executed)


    return continueWith(response, annotations)
  }
}