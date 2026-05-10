import burp.api.montoya.MontoyaApi
import burp.api.montoya.proxy.http.InterceptedResponse
import burp.api.montoya.proxy.http.ProxyResponseHandler
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction.continueWith
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction

class StripperProxyResponseHandler(
  var montoyaApi: MontoyaApi
) : ProxyResponseHandler {
  override fun handleResponseReceived(interceptedResponse: InterceptedResponse?): ProxyResponseReceivedAction? {
    if (interceptedResponse == null) return null

    val url = utils.Url.clean(interceptedResponse.initiatingRequest().url())
    val scope = utils.Settings.scope(montoyaApi)

    val response = interceptedResponse
      .withStatusCode(interceptedResponse.statusCode())

    val annotations = interceptedResponse.annotations()

    val isUrlInScope = utils.isUrlInScope(url, scope.scope)

    val settings = utils.Settings.load(montoyaApi)

    if (settings.responseEnabled && isUrlInScope) {
      val editedResponse = utils.Response.edit(
        montoyaApi,
        response,
        url,
        annotations,
        interceptedResponse.messageId(),
        "decrypt",
        "proxy"
      )
      return continueWith(editedResponse.response, editedResponse.annotations)
    }

    if (isUrlInScope) {
      return continueWith(interceptedResponse
        .withAddedHeader(K.HEADER.STRIPPER, K.Error.RESPONSE_NOT_SELECTED))
    }

    return continueWith(interceptedResponse, annotations)
  }

  override fun handleResponseToBeSent(interceptedResponse: InterceptedResponse?): ProxyResponseToBeSentAction? {
    if (interceptedResponse == null) return null
    return ProxyResponseToBeSentAction.continueWith(
      interceptedResponse.withRemovedHeader(K.HEADER.STRIPPER))
  }
}