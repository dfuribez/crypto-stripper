package models

import burp.api.montoya.persistence.PersistedList

data class StripperScope(
  val scope: PersistedList<String>,
  val black: PersistedList<String>,
  val force: PersistedList<String>
)
