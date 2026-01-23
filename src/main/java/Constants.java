public class Constants {
  public static String STRIPPER_SCOPE_LIST_KEY = "stripperScope";
  public static String STRIPPER_BLACK_LIST_KEY = "stripperBlackList";
  public static String STRIPPER_FORCE_INTERCEPT_LIST_KEY = "stripperForceIntercept";

  public static String REQUEST_SCRIPT_PATH_KEY = "requestScriptPath";
  public static String RESPONSE_SCRIPT_PATH_KEY = "responseScriptPath";

  public static String STRIPPER_HEADER = "X-Crypto-Stripper";

  public static String STRIPPER_ERROR_TEMPLATE = "Executing: \n %s %s %s \n Error: \n %s \n Scripts output: \n %s";

  public static String PROJECT_NODE_PATH_KEY = "projectNodePath";
  public static String PROJECT_PYTHON_PATH_KEY = "projectPythonPath";
  public static String GLOBAL_NODE_PATH_KEY = "globalNodePath";
  public static String GLOBAL_PYTHON_PATH_KEY = "globalPythonPath";

  public static String FIREPROXY_HEADER = "x-fire";

  public static String REQUEST_CHECKBOX_STATUS_KEY = "requestCheckboxStatus";
  public static String RESPONSE_CHECKBOX_STATUS_KEY = "responseCheckboxStatus";
  public static String FORCE_CHECKBOX_STATUS_KEY = "forceCheckboxStatus";

  public static String[] dangerousPseudoHeaders = {":scheme", ":method", ":path"};

  public static String X_STRIPPER_REQUEST_NOT_SELECTED = "Endpoint in scope but request script disabled";
  public static String X_STRIPPER_RESPONSE_NOT_SELECTED = "Endpoint in scope but response script disabled";
  public static String X_STRIPPER_ERROR = "An error occurred please check the preview for more details";

  public static String VERSION = """
      Crypto Stripper by Diego Uribe
      Version: v0.14-alpha
      Github: https://github.com/dfuribez/crypto-stripper
      Examples: https://github.com/dfuribez/crypto-stripper/wiki
      Playground: https://github.com/dfuribez/crypto-sripper-playground
      """;

  public static String SCRIPT_NOT_SUPORTED = "The selected script is not compatible with the current Stripper version."
      + "\nPlease update your script to avoid unexpected behavior.";

  public static String PASS_THROUGH = """
      {
          "enabled": true,
          "host": "^%s$",
          "port": "^%s$",
          "file":".*",
          "protocol": "any"
      }
      """;
}
