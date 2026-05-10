import burp.api.montoya.MontoyaApi
import kotlinx.serialization.json.Json
import models.ExecutorOutput
import models.JsonRequestResponse
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.io.path.createTempFile

object Executor {
  fun execute(
    montoyaApi: MontoyaApi,
    source: String,
    requestResponse: JsonRequestResponse
  ): ExecutorOutput {

    val script = if (source == "request") {
      montoyaApi.persistence().extensionData().getString(K.KEYS.REQUEST_SCRIPT_PATH) ?: ""
    } else {
      montoyaApi.persistence().extensionData().getString(K.KEYS.RESPONSE_SCRIPT_PATH) ?: ""
    }

    if (!utils.checkFileExists(script)) {
      return ExecutorOutput(error = "$script not a file")
    }

    val command = utils.getCommandFromPath(montoyaApi.persistence(), script)

    if (command == null) {
      return ExecutorOutput(
        error = "The selected script: "
          + script
          + " does not have a valid extension"
          + " or no binary selected"
      )
    }

    var temp: File? = null

    temp = createTempFile(prefix = "stripper_", suffix = ".json").toFile()
    val format = Json { encodeDefaults = true }
    val json = format.encodeToString(JsonRequestResponse.serializer(), requestResponse)
    temp.writeText(json, StandardCharsets.UTF_8)

    montoyaApi.logging().logToOutput("$command $script ${temp.absolutePath}")

    val process = ProcessBuilder(command, script, temp.absolutePath)
      .redirectErrorStream(false)
      .directory(File(script).parentFile)
      .start()

    val poutput = process.inputStream
      .bufferedReader(StandardCharsets.UTF_8)
      .use { it.readText() }

    val perror = process.errorStream
      .bufferedReader(StandardCharsets.UTF_8)
      .use { it.readText() }

    val decoded = try {
      String(
        Base64.getDecoder().decode(poutput.trim()),
        StandardCharsets.UTF_8
      )
    } catch (e: Exception) {
      return ExecutorOutput(error = "Error decoding base64. Don't use console.log()")
    }

    if (decoded.isBlank()) {
      return ExecutorOutput(
        error = "Script's output empty or null",
        stdErr = perror)
    }

    try {
      val o = Json.decodeFromString<ExecutorOutput>(decoded)

      if (!o.eventLog.isNullOrEmpty())
        montoyaApi.logging().raiseInfoEvent(o.eventLog)
      return o.copy(stdErr = perror)
    } catch (e: Exception) {
      montoyaApi.logging().logToOutput(e.toString())

      val error = StringBuilder()
        .append("--------- Extension errors ---------")
        .append(System.lineSeparator())
        .append("[+]")
        .append(e)
        .append(System.lineSeparator())

      if (temp != null) {
        if (!temp.delete()) {
          val message = "Could not delete: ${temp.absolutePath}"
          montoyaApi.logging().logToError(message)
            error.append(message)
              .append(System.lineSeparator())
        }
      }
      return ExecutorOutput(error = error.toString())
    }
  }
}