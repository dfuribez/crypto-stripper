import KUtils.Request.edit
import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.HighlightColor
import burp.api.montoya.proxy.http.InterceptedRequest
import burp.api.montoya.proxy.http.ProxyRequestHandler
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction.continueWith
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction.doNotIntercept
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction.intercept
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction

class StripperProxyRequestHandler(
  var montoyaApi: MontoyaApi,
  var stripperGui: MainTabGUI
) : ProxyRequestHandler {
  override fun handleRequestReceived(interceptedRequest: InterceptedRequest?): ProxyRequestReceivedAction? {
    if (interceptedRequest == null) return null
    val url = KUtils.Url.clean(interceptedRequest.url())
    val annotations = interceptedRequest.annotations()

    val scope =
      Utils.loadScope(montoyaApi.persistence().extensionData())

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
        KUtils.printError(montoyaApi, "StripperProxyRequestHandler.handleRequestReceived", e.toString())
      }
    }

    val isBlacklisted = stripperGui.enableBlackListcheckbox.isSelected
        && Utils.isUrlInScope(url, scope["blacklist"])
    val forceIntercept = Utils.isUrlInScope(url, scope["force"])
        && stripperGui.enableForceinterceptCheckbox.isSelected
    val isUrlInScope = Utils.isUrlInScope(url, scope["scope"])

    if (stripperGui.requestCheckBox.isSelected && isUrlInScope) {
      val editedRequest = edit(
        montoyaApi,
        request,
        annotations,
        interceptedRequest.messageId(),
        "decrypt",
        "proxy"
      );

      if (editedRequest.intercept == null) {
        if (isBlacklisted) return doNotIntercept(editedRequest.request, annotations)
        if (stripperGui.forceInterceptInScopeCheckbox.isSelected || forceIntercept) {
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
    if (stripperGui.enableForceinterceptCheckbox.isSelected
      && forceIntercept) return intercept(request, annotations)

    return continueWith(request, annotations)
  }

  override fun handleRequestToBeSent(interceptedRequest: InterceptedRequest?): ProxyRequestToBeSentAction? {
    if (interceptedRequest == null) return null
    return ProxyRequestToBeSentAction.continueWith(interceptedRequest.withRemovedHeader(K.HEADER.STRIPPER))
  }
}