package models

data class StripperSettings(
  val requestEnabled: Boolean,
  val responseEnabled: Boolean,
  val forceInScope: Boolean,

  val requestScriptPath: String,
  val responseScriptPath: String,
  val projectNodePath: String,
  val projectPythonPath: String,

  val globalNodePath: String,
  val globalPythonPath: String,

  val enableForce: Boolean,
  val enableBlack: Boolean
)
