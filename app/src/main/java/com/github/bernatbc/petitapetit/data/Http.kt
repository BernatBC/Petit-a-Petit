package com.github.bernatbc.petitapetit.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private const val TIMEOUT_MS = 15_000

suspend fun <T> httpGet(url: String, parse: (InputStream) -> T): T = withContext(Dispatchers.IO) {
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw IOException("HTTP ${connection.responseCode}")
        }
        parse(connection.inputStream)
    } finally {
        connection.disconnect()
    }
}
