import java.awt.*;


class K {
  object KEYS {
    const val SCOPE_LIST: String = "stripperScope"
    const val BLACK_LIST: String = "stripperBlackList"
    const val FORCE_INTERCEPT_LIST: String = "stripperForceIntercept"

    const val REQUEST_SCRIPT_PATH: String = "requestScriptPath"
    const val RESPONSE_SCRIPT_PATH: String = "responseScriptPath"

    const val PROJECT_NODE_PATH: String = "projectNodePath"
    const val PROJECT_PYTHON_PATH: String = "projectPythonPath"
    const val GLOBAL_NODE_PATH: String = "globalNodePath"
    const val GLOBAL_PYTHON_PATH: String = "globalPythonPath"

    const val REQUEST_CHECKBOX_STATUS = "requestCheckboxStatus";
    const val RESPONSE_CHECKBOX_STATUS = "responseCheckboxStatus";
    const val FORCE_CHECKBOX_STATUS = "forceCheckboxStatus";
  }

  object HEADER {
    const val FIREPROXY = "x-fire";
    const val STRIPPER = "X-Crypto-Stripper";
  }

  object Color {
    @JvmField
    val MAIN_BUTTON_BACKGROUND = Color(219, 97, 47);
    @JvmField
    val MAIN_BUTTON_FOREGROUND = Color(255, 255, 255);
  }

  object Error {
    const val TEMPLATE = "Executing: \n %s %s %s \n Error: \n %s \n Scripts output: \n %s"
    const val SCRIPT_NOT_SUPORTED = """The selected script is not compatible with the current Stripper version.
      \nPlease update your script to avoid unexpected behavior."""
    const val ERROR = "An error occurred please check the preview for more details"
    const val REQUEST_NOT_SELECTED = "Endpoint in scope but request script disabled";
    const val RESPONSE_NOT_SELECTED = "Endpoint in scope but response script disabled";
  }

  object Gen {
    const val VERSION = """
      Crypto Stripper by Diego Uribe
      Version: v0.14-alpha
      Github: https://github.com/dfuribez/crypto-stripper
      Examples: https://github.com/dfuribez/crypto-stripper/wiki
      Playground: https://github.com/dfuribez/crypto-sripper-playground
      """;

    const val PASS_THROUGH = """
      {
          "enabled": true,
          "host": "^%s$",
          "port": "^%s$",
          "file":".*",
          "protocol": "any"
      }
      """;

    @JvmField
    val dangerousPseudoHeaders = arrayOf(":scheme", ":method", ":path")
    @JvmField
    val TOOLS = arrayOf("proxy", "repeater", "intruder")

    const val PREVIEW_INFO_TEMPLATE = "%s %s";
  }

}