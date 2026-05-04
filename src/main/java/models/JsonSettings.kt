package models

import kotlinx.serialization.Serializable

@Serializable
data class JsonSettings(
  var enableRequest: Boolean = false,
  var enableResponse: Boolean = false,
  var enableForceIntercept: Boolean = false,

  var enableBlackList: Boolean = true,
  var enableForceList: Boolean = true,

  var scope: List<String> = emptyList(),
  var blackList: List<String> = emptyList(),
  var forceIntercept: List<String> = emptyList()
)
