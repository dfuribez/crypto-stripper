package utils

import java.net.URI

object Url {
  fun removePath(url: String): String? {
    try {
      val uri = URI(url)
      return URI(uri.scheme, uri.userInfo, uri.host, uri.port,
        null, null, null).toString()
    } catch (e: Exception) {
      return null
    }
  }

  fun clean(url: String): String {
    return url.split("?")[0]
  }
}