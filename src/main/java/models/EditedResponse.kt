package models

import burp.api.montoya.core.Annotations
import burp.api.montoya.http.message.responses.HttpResponse

data class EditedResponse(
  val response: HttpResponse,
  val annotations: Annotations
)
