package utils

import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

object Payloads{
  fun readFile(path: String, b64: Boolean, url: Boolean): ByteArray {
    return encode(File(path).readBytes(), b64, url)
  }

  private fun encode(content: ByteArray, b64: Boolean, url: Boolean): ByteArray {
    if (b64) return Base64.getEncoder().encode(content)
    if (url) return URLEncoder.encode(content.toString(StandardCharsets.UTF_8), StandardCharsets.UTF_8).toByteArray()
    return content
  }

  fun ByteArray.repeatToLength(len: Int): ByteArray {
    return ByteArray(len) { index -> this[index % size] }
  }

  fun generate(array: ByteArray, len: Int, repeatBytes: Boolean, b64: Boolean, url: Boolean): ByteArray {
    var total = len
    if (!repeatBytes) total = array.size * len
    return encode(array.repeatToLength(total), b64, url)
  }
}
