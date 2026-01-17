# Crypto Stripper v0.3
# Examples: https://github.com/dfuribez/crypto-stripper/wiki#examples

import base64
import json
import sys

#  Learn more: https://github.com/dfuribez/crypto-stripper/wiki/Stripper-scripts
def encrypt(body, headers, url_parameters, http_method, host, port, secure, path, status_code, reason_phrase, url, messageid, source):
    # implement your code here
    print("only print to stderr to debug", file=sys.stderr)
    print("the use of print() will cause the process to fail", file=sys.stderr)

    event_log = ""
    # setAnnotation(Color.NONE, "add your annotation here")

    return body, headers, url_parameters, http_method, host, port, secure, path, status_code, reason_phrase, event_log


#  Learn more: https://github.com/dfuribez/crypto-stripper/wiki/Stripper-scripts
def decrypt(body, headers, url_parameters, http_method, host, port, secure, path, status_code, reason_phrase, url, messageid, source):
    # implement your code here
    print("only print to stderr to debug", file=sys.stderr)
    print("the use of print() will cause the process to fail", file=sys.stderr)

    # setAnnotation(Color.NONE, "add your annotation here")
    event_log = ""
    intercept = False  # null: follow proxy configuration, true: force interception, false: does not intercept
    
    return body, headers, url_parameters, http_method, host, port, secure, path, status_code, reason_phrase, event_log, intercept


# DON'T MODIFY THIS
def print_json(body, headers, url_parameters, http_method, host, port, secure, path, status_code, reason_phrase, event_log=None, intercept=None):
    print(
        base64.b64encode(
            json.dumps({
                "body": body,
                "headers": headers,
                "urlParameters": url_parameters,
                "statusCode": status_code,
                "reasonPhrase": reason_phrase,
                "httpMethod": http_method,
                "path": path,
                "host": host,
                "port": port,
                "secure": secure,
                "version": 5,
                "eventLog": event_log,
                "intercept": intercept,
                "issue": issue,
                "annotation": annotation
                }
            ).encode("utf8")
        ).decode()
    )

def addIssue(name, detail, remediation, background, remediationBackground):
    global issue
    issue = {}
    issue["name"] = name
    issue["detail"] = detail
    issue["remediation"] = remediation
    issue["background"] = background
    issue["remediationBackground"] = remediationBackground


def setAnnotation(color, note):
    global annotation
    annotation = {}
    annotation["color"] = color
    annotation["note"] = note


class Color():
  BLUE = "BLUE"
  CYAN = "CYAN"
  GRAY = "GRAY"
  GREEN = "GREEN"
  MAGENTA = "MAGENTA"
  NONE = "NONE"
  ORANGE = "ORANGE"
  PINK = "PINK"
  RED = "RED"
  YELLOW = "YELLOW"


annotation = None
issue = None

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
    source = json_content.get("toolSource")
    host = json_content.get("host")
    port = json_content.get("port")
    secure = json_content.get("secure")

    if (json_content["action"] == "encrypt"):
        encrypted = encrypt(body, headers, url_parameters, http_method, host, port, secure, path, status_code, reason_phrase, url, messageid, source)
        print_json(*encrypted)
    else:
        decrypted = decrypt(body, headers, url_parameters, http_method, host, port, secure, path, status_code, reason_phrase, url, messageid, source)
        print_json(*decrypted)
