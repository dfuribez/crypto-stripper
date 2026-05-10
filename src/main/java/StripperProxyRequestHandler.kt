import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.HighlightColor
import burp.api.montoya.proxy.http.InterceptedRequest
import burp.api.montoya.proxy.http.ProxyRequestHandler
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction.continueWith
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction.doNotIntercept
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction.intercept
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction

class StripperProxyRequestHandler(var montoyaApi: MontoyaApi) : ProxyRequestHandler {
  override fun handleRequestReceived(interceptedRequest: InterceptedRequest?): ProxyRequestReceivedAction? {
    if (interceptedRequest == null) return null
    val url = KUtils.Url.clean(interceptedRequest.url())
    val annotations = interceptedRequest.annotations()

    val scope = utils.Settings.scope(montoyaApi)

    var request = interceptedRequest
      .withMethod(interceptedRequest.method())

    if (interceptedRequest.hasHeader(K.HEADER.FIREPROXY)) {
      val headerValue = interceptedRequest.headerValue(K.HEADER.FIREPROXY)
      val color = headerValue.substringBefore(",")
      val containerName = headerValue.substringAfter(",")

      try {
        annotations.setNotes(containerName)
        annotations.setHighlightColor(HighlightColor.valueOf(color.uppercase()))
      } catch (e : Exception) {
        utils.printError(montoyaApi, "StripperProxyRequestHandler.handleRequestReceived", e.toString())
      }
    }

    val settings = utils.Settings.load(montoyaApi)

    val isBlacklisted = settings.enableBlack && utils.isUrlInScope(url, scope.black)
    val forceIntercept = utils.isUrlInScope(url, scope.force) && settings.enableForce
    val isUrlInScope = utils.isUrlInScope(url, scope.scope)

    if (settings.requestEnabled && isUrlInScope) {
      val editedRequest = utils.Request.edit(
        montoyaApi,
        request,
        annotations,
        interceptedRequest.messageId(),
        "decrypt",
        "proxy"
      );

      if (editedRequest.intercept == null) {
        if (isBlacklisted) return doNotIntercept(editedRequest.request, annotations)
        if (settings.forceInScope || forceIntercept) {
          return intercept(editedRequest.request, annotations)
        }
        return continueWith(editedRequest.request, annotations)
      }

      if (editedRequest.intercept) {
        return intercept(editedRequest.request, annotations)
      }
      return doNotIntercept(editedRequest.request, annotations)
    }

    if (isUrlInScope) {
      request = request.withHeader(K.HEADER.STRIPPER, K.Error.REQUEST_NOT_SELECTED)
    }

    if (isBlacklisted) return doNotIntercept(request, annotations)
    if (settings.forceInScope && forceIntercept) return intercept(request, annotations)

    return continueWith(request, annotations)
  }

  override fun handleRequestToBeSent(interceptedRequest: InterceptedRequest?): ProxyRequestToBeSentAction? {
    if (interceptedRequest == null) return null
    return ProxyRequestToBeSentAction.continueWith(
      interceptedRequest.withRemovedHeader(K.HEADER.STRIPPER))
  }
}