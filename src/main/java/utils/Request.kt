package utils

import K
import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.Annotations
import burp.api.montoya.core.ByteArray
import burp.api.montoya.http.HttpService
import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.params.HttpParameter
import burp.api.montoya.http.message.params.HttpParameterType
import burp.api.montoya.http.message.params.ParsedHttpParameter
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import models.EditedRequest
import models.ExecutorOutput
import models.JsonRequestResponse
import java.nio.charset.StandardCharsets
import java.util.*

object Request {
  fun edit(
    montoyaApi: MontoyaApi,
    request: HttpRequest,
    annotations: Annotations,
    messageId: Int,
    action: String,
    toolName: String
  ) : EditedRequest {
    val url = utils.Url.clean(request.url())
    var newAnnotations: Annotations? = null

    val ready = JsonRequestResponse(
      body = String(request.body().bytes, StandardCharsets.UTF_8),
      headers = headersToArray(request.headers()),
      url = request.url(),
      urlParameters = parametersToArray(request.parameters(HttpParameterType.URL)),
      messageId = messageId,
      method = request.method(),
      path = request.path(),
      toolSource = toolName,
      host = request.httpService().host(),
      port = request.httpService().port(),
      secure = request.httpService().secure(),
      action = action
    )
    val executed = Executor.execute(montoyaApi, "request", ready)
    val edited = executorToHttpRequest(request, executed)

    if (!executed.issue.isNullOrEmpty()) {
      setIssue(
        montoyaApi,
        executed.issue,
        url,
        request,
        HttpResponse.httpResponse()
      )
    }

    if (!executed.annotation.isNullOrEmpty()) {
      newAnnotations = setAnnotation(
        annotations.notes(),
        executed.annotation["color"],
        executed.annotation["note"]
      )
    }
    return EditedRequest(
      request = edited,
      annotations = newAnnotations ?: annotations,
      intercept = executed.intercept,
      executed = executed
    )
  }

  private fun executorToHttpRequest(request: HttpRequest, output: ExecutorOutput): HttpRequest {
    if (!output.error.isEmpty()) {
      return request
        .withHeader(K.HEADER.STRIPPER, K.Error.ERROR)
    }

    var modified = request
      .withService(HttpService.httpService(output.host, output.port, output.secure))
      .withPath(output.path)
      .withRemovedParameters(request.parameters(HttpParameterType.URL))
      .withMethod(output.httpMethod)

    // avoids kettling
    for (header in request.headers()) {
      if (!K.Gen.dangerousPseudoHeaders.contains(header.name())) {
        modified = modified.withRemovedHeader(header.name())
      }
    }

    modified = modified.withAddedHeaders(output.headers.map { HttpHeader.httpHeader(it) })

    return modified
      .withBody(ByteArray.byteArray(*output.body.toByteArray(StandardCharsets.UTF_8)))
      .withAddedParameters(listToUrlParams(output.urlParameters))
      .withHeader(K.HEADER.STRIPPER, "true")
      .withUpdatedHeader("Host", output.host)
  }

  private fun parametersToArray(parameters: MutableList<ParsedHttpParameter>): List<HashMap<String, String>> {
    return parameters.map {
      hashMapOf("name" to it.name(), "value" to it.value())
    }
  }

  fun listToUrlParams(
    urlParametersList: List<Map<String, String>>
  ): List<HttpParameter?> {
    return urlParametersList.map {
        HttpParameter.urlParameter(it["name"], it["value"])
    }
  }
}
