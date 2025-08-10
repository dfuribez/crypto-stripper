// Crypto Stripper
// Examples: https://github.com/dfuribez/crypto-stripper/wiki#examples

let fs = require("fs");


// Function that performs the decryption
// Learn more: https://github.com/dfuribez/crypto-stripper/wiki/Stripper-scripts
async function decrypt(body, headers, urlParameters, httpMethod, host, port, secure, path, statusCode, reasonPhrase, url, messageId, source) {
  console.error("only use console.error to debug")
  console.error("the use of console.log will cause the process to fail")

  return [body, headers, urlParameters, httpMethod, host, port, secure, path, statusCode, reasonPhrase]
}


// Function that perform encryption
// Learn more: https://github.com/dfuribez/crypto-stripper/wiki/Stripper-scripts
async function encrypt(body, headers, urlParameters, httpMethod, host, port, secure, path, statusCode, reasonPhrase, url, messageId, source) {
  console.error("only use console.error to debug")
  console.error("the use of console.log will cause the process to fail")

  return [body, headers, urlParameters, httpMethod, host, port, secure, path, statusCode, reasonPhrase];
}


// DON'T TOUCH THIS
function printJSON(body, headers, urlParameters, httpMethod, host, port, secure, path, statusCode, reasonPhrase) {
  console.log(
    Buffer.from(
      JSON.stringify(
        {
          body: body,
          headers: headers,
          urlParameters: urlParameters,
          statusCode: statusCode,
          reasonPhrase: reasonPhrase,
          httpMethod: httpMethod,
          path: path,
          version: 2,
          host: host,
          port: port,
          secure: secure
        }
      )
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
  var source = jsonData.toolSource
  var host = jsonData.host
  var port = jsonData.port
  var secure = jsonData.secure

  if (jsonData.action == "encrypt") {
    var enc = await encrypt(body, headers, urlParameters, httpMethod, host, port, secure, path, statusCode, reasonPhrase, url, messageId, source)
    printJSON(...enc)
  } else {
    var dec = await decrypt(body, headers, urlParameters, httpMethod, host, port, secure, path, statusCode, reasonPhrase, url, messageId, source)
    printJSON(...dec)
  }
}

main()
