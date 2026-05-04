import burp.api.montoya.MontoyaApi
import burp.api.montoya.proxy.http.InterceptedResponse
import burp.api.montoya.proxy.http.ProxyResponseHandler
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction
import burp.api.montoya.proxy.http.ProxyResponseReceivedAction.continueWith
import burp.api.montoya.proxy.http.ProxyResponseToBeSentAction
import models.EditedResponse

class StripperProxyResponseHandler(
  var montoyaApi: MontoyaApi
) : ProxyResponseHandler {
  override fun handleResponseReceived(interceptedResponse: InterceptedResponse?): ProxyResponseReceivedAction? {
    if (interceptedResponse == null) return null

    val url = KUtils.Url.clean(interceptedResponse.initiatingRequest().url())
    val scope = Utils2.Settings.scope(montoyaApi)

    val response = interceptedResponse
      .withStatusCode(interceptedResponse.statusCode())

    val annotations = interceptedResponse.annotations()

    val isUrlInScope = Utils2.isUrlInScope(url, scope.scope)

    val settings = Utils2.Settings.load(montoyaApi)

    if (settings.responseEnabled && isUrlInScope) {
      val editedResponse = KUtils.Response.edit(
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