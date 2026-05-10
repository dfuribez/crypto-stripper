package utils

import java.io.File

object Payloads{
  fun readFile(path: String): ByteArray {
    return File(path).readBytes()
  }

  fun generate() {}

}