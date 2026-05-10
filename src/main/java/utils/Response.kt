package utils

import K
import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.Annotations
import burp.api.montoya.core.ByteArray
import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import com.google.gson.Gson
import models.EditedResponse
import models.ExecutorOutput
import models.JsonRequestResponse
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
    val ready = JsonRequestResponse(
      body = String(response.body().bytes, StandardCharsets.UTF_8),
      action = action,
      headers = headersToArray(response.headers()),
      url = url,
      messageId = messageId,
      statusCode = response.statusCode(),
      reasonPhrase = response.reasonPhrase(),
      toolSource = toolName
    )

    val executed = Executor.execute(montoyaApi, "response", ready);
    val response = executorToHttpResponse(response, executed)

    if (executed.issue != null) {
      setIssue(
        montoyaApi,
        executed.issue,
        url,
        HttpRequest.httpRequest(),
        response
      )
    }

    if (executed.annotation != null) {
      newAnnotations = setAnnotation(
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

  private fun executorToHttpResponse(response: HttpResponse, output: ExecutorOutput): HttpResponse {
    if (!output.error.isEmpty()) {
      return response
        .withAddedHeader(K.HEADER.STRIPPER, K.Error.ERROR)
    }

    val modified = response
      .withRemovedHeaders(response.headers())
      .withAddedHeaders(output.headers.map { HttpHeader.httpHeader(it) })
      .withStatusCode(output.statusCode)
      .withReasonPhrase(output.reasonPhrase)

    return modified
      .withBody(ByteArray.byteArray(*output.body.toByteArray(StandardCharsets.UTF_8)))
      .withAddedHeader(K.HEADER.STRIPPER, "true")
  }
}