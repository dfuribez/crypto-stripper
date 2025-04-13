public class Constants {
  public static String STRIPPER_SCOPE_LIST_KEY = "stripperScope";
  public static String STRIPPER_BLACK_LIST_KEY = "stripperBlackList";
  public static String STRIPPER_FORCE_INTERCEPT_LIST_KEY = "stripperForceIntercept";

  public static String REQUEST_SCRIPT_PATH_KEY = "requestScriptPath";
  public static String RESPONSE_SCRIPT_PATH_KEY = "responseScriptPath";

  public static String STRIPPER_HEADER = "X-Stripper";

  public static String STRIPPER_ERROR_TEMPLATE = "Executing %s %s %s \n Error: \n %s \n Scripts output: %s";

  public static String PROJECT_NODE_PATH_KEY = "projectNodePath";
  public static String PROJECT_PYTHON_PATH_KEY = "projectPythonPath";
  public static String GLOBAL_NODE_PATH_KEY = "globalNodePath";
  public static String GLOBAL_PYTHON_PATH_KEY = "globalPythonPath";

  public static String FIREPROXY_HEADER = "x-fire";

  public static String REQUEST_CHECKBOX_STATUS_KEY = "requestCheckboxStatus";
  public static String RESPONSE_CHECKBOX_STATUS_KEY = "responseCheckboxStatus";
  public static String FORCE_CHECKBOX_STATUS_KEY = "forceCheckboxStatus";

  public static String JS_TEMPLATE = """
var jsonData = JSON.parse(atob(process.argv[2]))

var body = jsonData.body
var headers = JSON.parse(jsonData.headers)
var urlParameters = JSON.parse(jsonData.urlParameters)
var url = jsonData.url
var messageId = jsonData.messageId


if (jsonData.action == "encrypt") {
    var enc =  encrypt(body, headers, urlParameters, url, messageId)
    prepare_return(enc[0], enc[1], enc[2], enc[3])
} else {
    var dec = decrypt(body, headers, urlParameters, url, messageId)
    prepare_return(dec[0], dec[1], dec[2])
}

function prepare_return(body, headers, params, replaceResponse=false) {
    console.log(
        Buffer.from(
            JSON.stringify({
                body: body,
                headers: headers,
                urlParameters: params,
                replaceResponse: replaceResponse
            })
        ).toString("base64")
    )
}


// Function that performs the decryption
function decrypt(body, headers, params, url, messageId) {
    console.error("only use console.error to debug")
    console.error("the use of console.log will cause the process to fail")
    return [body, headers, params]
}


// Function that perform encryption
function encrypt(body, headers, params, url, messageId) {
    let replaceResponse = false;  // only used in responses
    return [body, headers, params, replaceResponse];
}

      """;

  public static String PYTHON_TEMPLATE = """
      import base64
      import json
      import sys
      
      
      def prepare_return(body, headers, urlparameters, replaceresponse=False):
          print(
              base64.b64encode(
                  json.dumps({
                      "body": body,
                      "headers": headers,
                      "urlParameters": urlparameters,
                      "replaceResponce": replaceresponse
                      }
                  ).encode("utf8")
              ).decode()
          )
      
      
      def encrypt(body, headers, urlparameters, url, messageid):
          # implement your code here
          return body, headers, urlparameters
      
      
      def decrypt(body, headers, urlparameters, url, messageid):
          # implement your code here
          replaceresponce = False
          return body, headers, urlparameters, replaceresponce
      
      
      jsonData = json.loads(base64.b64decode(sys.argv[1]))
      
      body = jsonData["body"]
      headers = json.loads(jsonData["headers"])
      url = jsonData["url"]
      urlparameters = json.loads(jsonData["urlParameters"])
      messageid = jsonData["messageId"]
      
      if (jsonData["action"] == "encrypt"):
          encrypted = encrypt(body, headers, urlparameters, url, messageid)
          prepare_return(encrypted[0], encrypted[1], encrypted[2])
      else:
          decrypted = decrypt(body, headers, urlparameters, url, messageid)
          prepare_return(decrypted[0], decrypted[1], decrypted[2], decrypted[3])
      
      """;
}
