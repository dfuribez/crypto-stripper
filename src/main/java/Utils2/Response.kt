package Utils2

import Executor
import K
import KUtils.headersToArray
import Utils
import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.Annotations
import burp.api.montoya.core.ByteArray
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import com.google.gson.Gson
import models.EditedResponse
import models.ExecutorOutput
import java.nio.charset.StandardCharsets

object Response {
  fun edit(
    montoyaApi: MontoyaApi,
    response: HttpResponse,
    url: String,
    annotations: Annotations,
    messageId: Int,
    action: String,
    toolName: String
  ): EditedResponse {
    var newAnnotations: Annotations? = null
    val ready = prepareResponseForExecutor(response, url, messageId, toolName)

    val executed = Executor.execute(montoyaApi, action, "response", ready);
    val response = executorToHttpResponse(response, executed)

    if (executed.issue != null) {
      Utils.setIssue(
        montoyaApi,
        executed.issue,
        url,
        HttpRequest.httpRequest(),
        response
      )
    }

    if (executed.annotation != null) {
      newAnnotations = Utils.setAnnotation(
        annotations.notes(),
        executed.annotation["color"],
        executed.annotation["note"]
      )
    }

    return EditedResponse(
      response = response,
      annotations = newAnnotations ?: annotations,
      executed = executed
    )
  }

  private fun prepareResponseForExecutor(
    response: HttpResponse, url: String?, messageId: Int, source: String?
  ): HashMap<String?, String?> {
    val result = HashMap<String?, String?>()

    val headers = Gson().toJson(headersToArray(response.headers()))

    val urlParameters = Gson().toJson(null)

    result.put("body", String(response.body().getBytes(), StandardCharsets.UTF_8))
    result.put("headers", headers)
    result.put("urlParameters", urlParameters)
    result.put("url", url)
    result.put("messageId", messageId.toString())
    result.put("statusCode", response.statusCode().toInt().toString())
    result.put("reasonPhrase", response.reasonPhrase())
    result.put("toolSource", source)

    return result
  }

  private fun executorToHttpResponse(response: HttpResponse, output: ExecutorOutput): HttpResponse {
    if (output.error != null && !output.error.isEmpty()) {
      return response
        .withAddedHeader(K.HEADER.STRIPPER, K.Error.ERROR)
    }

    val modified = response
      .withRemovedHeaders(response.headers())
      .withAddedHeaders(Utils.listToHttpHeaders(output.headers))
      .withStatusCode(output.statusCode)
      .withReasonPhrase(output.reasonPhrase)

    return modified
      .withBody(ByteArray.byteArray(*output.body.toByteArray(StandardCharsets.UTF_8)))
      .withAddedHeader(K.HEADER.STRIPPER, "true")
  }
}