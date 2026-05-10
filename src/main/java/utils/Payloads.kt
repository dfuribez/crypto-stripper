package utils

import java.io.File
import java.util.Base64

object Payloads{
  fun readFile(path: String, b64: Boolean): ByteArray {
    return encode(File(path).readBytes(), b64)
  }

  private fun encode(content: ByteArray, b64: Boolean): ByteArray {
    if (b64) return Base64.getEncoder().encode(content)
    return content
  }

  fun generate() {}
}