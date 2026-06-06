package models

import kotlinx.serialization.Serializable

@Serializable
data class JsonRequestResponse(
  val body: String = "",
  val headers: List<String?> = ArrayList(),
  val url: String = "",
  val urlParameters: List<Map<String, String>> = ArrayList(),
  val messageId: Int = -1,
  val method: String = "",
  val path: String = "",
  val toolSource: String = "",
  val host: String = "",
  val port: Int = 0,
  val secure: Boolean = true,
  val action: String,
  val statusCode: Short = 0,
  val reasonPhrase: String = ""
)
