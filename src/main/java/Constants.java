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
      Version: v0.7-alpha
      Github: https://github.com/dfuribez/crypto-stripper
      Examples: https://github.com/dfuribez/crypto-stripper/wiki
      Playground: https://github.com/dfuribez/crypto-sripper-playground
      """;

  public static String JS_TEMPLATE = """
// Crypto Stripper
// Examples: https://github.com/dfuribez/crypto-stripper/wiki#examples

let fs = require("fs");

// Function that performs the decryption
async function decrypt(body, headers, urlParameters, httpMethod, path, statusCode, reasonPhrase, url, messageId) {
    let replaceResponse = false;  // only used in responses
    console.error("only use console.error to debug")
    console.error("the use of console.log will cause the process to fail")

    return [body, headers, urlParameters, httpMethod, path, statusCode, reasonPhrase, replaceResponse]
}


// Function that perform encryption
async function encrypt(body, headers, urlParameters, httpMethod, path, statusCode, reasonPhrase, url, messageId) {
    console.error("only use console.error to debug")
    console.error("the use of console.log will cause the process to fail")

    return [body, headers, urlParameters, httpMethod, path, statusCode, reasonPhrase];
}


// DON'T TOUCH THIS
function printJSON(body, headers, urlParameters, httpMethod, path, statusCode, reasonPhrase, replaceResponse=false) {
    console.log(
        Buffer.from(
            JSON.stringify({
                body: body,
                headers: headers,
                urlParameters: urlParameters,
                replaceResponse: replaceResponse,
                statusCode: statusCode,
                reasonPhrase: reasonPhrase,
                httpMethod: httpMethod,
                path: path
            })
        ).toString("base64")
    )
}

async function main() {
    console.error(process.argv[2])
    var jsonData = JSON.parse(fs.readFileSync(process.argv[2]).toString())

    var body = jsonData.body
    var headers = JSON.parse(jsonData.headers)
    var urlParameters = JSON.parse(jsonData.urlParameters)
    var url = jsonData.url
    var messageId = jsonData.messageId
    var statusCode = jsonData.statusCode
    var reasonPhrase = jsonData.reasonPhrase
    var httpMethod = jsonData.httpMethod
    var path = jsonData.path

    if (jsonData.action == "encrypt") {
        var enc = await encrypt(body, headers, urlParameters, httpMethod, path, statusCode, reasonPhrase, url, messageId)
        printJSON(...enc)
    } else {
        var dec = await decrypt(body, headers, urlParameters, httpMethod, path, statusCode, reasonPhrase, url, messageId)
        printJSON(...dec)
    }
}

main()

  """;

  public static String PYTHON_TEMPLATE = """
# Crypto Stripper v0.3
# Examples: https://github.com/dfuribez/crypto-stripper/wiki#examples

import base64
import json
import sys

def encrypt(body, headers, url_parameters, http_method, path, status_code, reason_phrase, url, messageid):
    # implement your code here
    print("only print to stderr to debug", file=sys.stderr)
    print("the use of print() will cause the process to fail", file=sys.stderr)

    return body, headers, url_parameters, http_method, path, status_code, reason_phrase


def decrypt(body, headers, url_parameters, http_method, path, status_code, reason_phrase, url, messageid):
    # implement your code here
    replace_response = False
    print("only print to stderr to debug", file=sys.stderr)
    print("the use of print() will cause the process to fail", file=sys.stderr)
    return body, headers, url_parameters, http_method, path, status_code, reason_phrase, replace_response


# DON'T MODIFY THIS
def print_json(body, headers, url_parameters, http_method, path, status_code, reason_phrase, replace_response=False):
    print(
        base64.b64encode(
            json.dumps({
                "body": body,
                "headers": headers,
                "urlParameters": url_parameters,
                "replaceResponse": replace_response,
                "statusCode": status_code,
                "reasonPhrase": reason_phrase,
                "httpMethod": http_method,
                "path": path
                }
            ).encode("utf8")
        ).decode()
    )


with open(sys.argv[1]) as file:
    json_content = json.load(file)

    body = json_content["body"]
    headers = json.loads(json_content["headers"])
    url = json_content["url"]
    url_parameters = json.loads(json_content["urlParameters"])
    messageid = json_content["messageId"]
    status_code = json_content.get("statusCode", 0)
    reason_phrase = json_content.get("reasonPhrase", "")
    http_method = json_content.get("httpMethod", "")
    path = json_content.get("path", "")

    if (json_content["action"] == "encrypt"):
        encrypted = encrypt(body, headers, url_parameters, http_method, path, status_code, reason_phrase, url, messageid)
        print_json(*encrypted)
    else:
        decrypted = decrypt(body, headers, url_parameters, http_method, path, status_code, reason_phrase, url, messageid)
        print_json(*decrypted)

  """;
}
