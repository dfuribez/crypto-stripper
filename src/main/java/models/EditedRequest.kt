package models

import burp.api.montoya.core.Annotations
import burp.api.montoya.http.message.requests.HttpRequest

data class EditedRequest(
  val request: HttpRequest,
  val annotations: Annotations,
  val intercept: Boolean?
)
