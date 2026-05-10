package utils

import Executor
import K
import KUtils
import KUtils.Url.clean
import KUtils.headersToArray
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
import com.google.gson.Gson
import models.EditedRequest
import models.ExecutorOutput
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
    val url = KUtils.Url.clean(request.url())
    var newAnnotations: Annotations? = null

    val ready = prepareRequestForExecutor(request, messageId, toolName)
    val executed = Executor.execute(montoyaApi, action, "request", ready)
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
    if (output.error != null && !output.error.isEmpty()) {
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

  private fun prepareRequestForExecutor(
    request: HttpRequest, messageId: Int, source: String?
  ): HashMap<String?, String?> {
    val result = HashMap<String?, String?>()

    val headers = Gson().toJson(headersToArray(request.headers()))

    val urlParameters = Gson().toJson(
      parametersToArray(request.parameters(HttpParameterType.URL))
    )

    result.put("body", String(request.body().getBytes(), StandardCharsets.UTF_8))
    result.put("headers", headers)
    result.put("urlParameters", urlParameters)
    result.put("url", clean(request.url()))
    result.put("messageId", messageId.toString())
    result.put("httpMethod", request.method())
    result.put("path", request.path())
    result.put("toolSource", source)
    result.put("host", request.httpService().host())
    result.put("port", request.httpService().port().toString())
    result.put("secure", request.httpService().secure().toString())

    return result
  }

  private fun parametersToArray(parameters: MutableList<ParsedHttpParameter>): List<HashMap<String, String>> {
    return parameters.map {
      hashMapOf("name" to it.name(), "value" to it.value())
    }
  }

  fun listToUrlParams(
    urlParametersList: List<HashMap<String, String>>
  ): List<HttpParameter?> {
    return urlParametersList.map {
        HttpParameter.urlParameter(it["name"], it["value"])
    }
  }
}
