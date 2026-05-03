package Utils2

import K
import burp.api.montoya.MontoyaApi
import burp.api.montoya.persistence.PersistedList
import kotlinx.serialization.json.Json
import models.JsonSettings
import models.StripperScope
import models.StripperSettings
import java.awt.Component
import java.io.File
import java.io.FileReader
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object Settings {
  fun load(montoyaApi: MontoyaApi): StripperSettings {
    val extensionData = montoyaApi.persistence().extensionData()
    val preferences = montoyaApi.persistence().preferences()
    return StripperSettings(
      requestEnabled = extensionData.getBoolean(K.KEYS.REQUEST_CHECKBOX_STATUS) ?: false,
      responseEnabled = extensionData.getBoolean(K.KEYS.RESPONSE_CHECKBOX_STATUS) ?: false,
      forceInScope =extensionData.getBoolean(K.KEYS.FORCE_CHECKBOX_STATUS) ?: false,
      requestScriptPath = extensionData.getString(K.KEYS.REQUEST_SCRIPT_PATH) ?: "",
      responseScriptPath = extensionData.getString(K.KEYS.RESPONSE_SCRIPT_PATH) ?: "",
      projectNodePath = extensionData.getString(K.KEYS.PROJECT_NODE_PATH) ?: "",
      projectPythonPath = extensionData.getString(K.KEYS.PROJECT_PYTHON_PATH) ?: "",
      globalNodePath = preferences.getString(K.KEYS.GLOBAL_NODE_PATH) ?: "",
      globalPythonPath = preferences.getString(K.KEYS.GLOBAL_PYTHON_PATH) ?: "",
      enableForce = extensionData.getBoolean(K.KEYS.ENABLE_FORCE) ?: true,
      enableBlack = extensionData.getBoolean(K.KEYS.ENABLE_BLACK) ?: true,
    )
  }

  fun clear(montoyaApi: MontoyaApi) {
    montoyaApi.persistence().extensionData().setBoolean(K.KEYS.REQUEST_CHECKBOX_STATUS, true);
    montoyaApi.persistence().extensionData().setBoolean(K.KEYS.RESPONSE_CHECKBOX_STATUS, true);
    montoyaApi.persistence().extensionData().setBoolean(K.KEYS.FORCE_CHECKBOX_STATUS, false);

    montoyaApi.persistence().extensionData().setBoolean(K.KEYS.ENABLE_FORCE, true);
    montoyaApi.persistence().extensionData().setBoolean(K.KEYS.ENABLE_BLACK, true);


    montoyaApi.persistence().extensionData().setString(K.KEYS.RESPONSE_SCRIPT_PATH, "");
    montoyaApi.persistence().extensionData().setString(K.KEYS.REQUEST_SCRIPT_PATH, "");

    montoyaApi.persistence().extensionData().setString(K.KEYS.PROJECT_NODE_PATH, "");
    montoyaApi.persistence().extensionData().setString(K.KEYS.PROJECT_PYTHON_PATH, "");

    montoyaApi.persistence().extensionData().setStringList(
      K.KEYS.SCOPE_LIST, PersistedList.persistedStringList());
    montoyaApi.persistence().extensionData().setStringList(
      K.KEYS.BLACK_LIST, PersistedList.persistedStringList());
    montoyaApi.persistence().extensionData().setStringList(
      K.KEYS.FORCE_INTERCEPT_LIST, PersistedList.persistedStringList());

    montoyaApi.persistence().preferences().setString(K.KEYS.GLOBAL_PYTHON_PATH, "");
    montoyaApi.persistence().preferences().setString(K.KEYS.GLOBAL_NODE_PATH, "");
  }

  fun importFromJson(montoyaApi: MontoyaApi, path: String) {
    val jsonFile = FileReader(path).readText()
    val settings = Json.decodeFromString<JsonSettings>(jsonFile)

    val persistedScope = PersistedList.persistedStringList()
    persistedScope.addAll(settings.scope.filter { isValidRegex(it) })

    val persistedBlack = PersistedList.persistedStringList()
    persistedBlack.addAll(settings.blackList.filter { isValidRegex(it) })

    val persistedForce = PersistedList.persistedStringList()
    persistedForce.addAll(settings.forceIntercept.filter { isValidRegex(it) })

    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.REQUEST_CHECKBOX_STATUS, settings.enableRequest)
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.RESPONSE_CHECKBOX_STATUS, settings.enableResponse)
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.FORCE_CHECKBOX_STATUS, settings.enableForceIntercept)
    montoyaApi.persistence().extensionData().setStringList(
      K.KEYS.SCOPE_LIST, persistedScope)
    montoyaApi.persistence().extensionData().setStringList(
      K.KEYS.BLACK_LIST, persistedBlack)
    montoyaApi.persistence().extensionData().setStringList(
      K.KEYS.FORCE_INTERCEPT_LIST, persistedForce)
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.ENABLE_BLACK, settings.enableBlackList)
    montoyaApi.persistence().extensionData().setBoolean(
      K.KEYS.ENABLE_FORCE, settings.enableForceList)
  }

  fun exportToJson(montoyaApi: MontoyaApi, path: String) {
    val json = Json {
      prettyPrint = true
      encodeDefaults = true
    }

    val settings = load(montoyaApi)
    val scope = scope(montoyaApi)

    montoyaApi.logging().logToOutput(settings.toString())

    val jsonSettings = JsonSettings(
      enableRequest = settings.requestEnabled,
      enableResponse = settings.responseEnabled,
      enableForceIntercept = settings.forceInScope,
      scope = scope.scope,
      blackList = scope.black,
      forceIntercept = scope.force,
      enableBlackList = settings.enableBlack,
      enableForceList = settings.enableForce
    )

    montoyaApi.logging().logToOutput(jsonSettings.toString())

    val jsonString = json.encodeToString(JsonSettings.serializer(), jsonSettings)
    File(path).writeText(jsonString)
  }

}

fun scope(montoyaApi: MontoyaApi) : StripperScope {
  val extensionData = montoyaApi.persistence().extensionData()
  val stripperScope = extensionData.getStringList(K.KEYS.SCOPE_LIST)
    ?: PersistedList.persistedStringList()
  val stripperBlack = extensionData.getStringList(K.KEYS.BLACK_LIST)
    ?: PersistedList.persistedStringList()
  val stripperForce = extensionData.getStringList(K.KEYS.FORCE_INTERCEPT_LIST)
    ?: PersistedList.persistedStringList()
  return StripperScope(
    scope = stripperScope,
    black = stripperBlack,
    force = stripperForce
  )
}

