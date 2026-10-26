package com.github.bernatbc.petitapetit.data.translation

import com.github.bernatbc.petitapetit.data.httpGet
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder

class MyMemoryTranslator : WordTranslator {

    override suspend fun translate(text: String): String {
        val query = URLEncoder.encode(text, "UTF-8")
        return httpGet("$API_URL?q=$query&langpair=fr%7Cen", ::parse)
    }

    internal fun parse(input: InputStream): String {
        val root = JSONObject(input.bufferedReader().use { it.readText() })
        if (root.optInt("responseStatus") != 200 || root.optBoolean("quotaFinished")) {
            throw IOException("Translation unavailable")
        }
        return root.getJSONObject("responseData").getString("translatedText").trim()
            .takeIf { it.isNotEmpty() }
            ?: throw IOException("Empty translation")
    }

    private companion object {
        const val API_URL = "https://api.mymemory.translated.net/get"
    }
}
