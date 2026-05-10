package models

import kotlinx.serialization.Serializable

@Serializable
data class ExecutorOutput(
  val body: String = "",
  val headers: List<String> = ArrayList(),
  val urlParameters: List<Map<String, String>> = ArrayList(),

  val host: String = "",
  val port: Int = 0,
  val secure: Boolean = true,

  val error: String = "",
  val stdErr: String = "",

  val statusCode: Short = 0,
  val reasonPhrase: String = "",
  val path: String = "",
  val httpMethod: String = "",

  val version: Short = 0,

  val eventLog: String? = null,
  val intercept: Boolean? = null,
  val issue: HashMap<String?, String?>? = null,
  val annotation: HashMap<String?, String?>? = null,
)
