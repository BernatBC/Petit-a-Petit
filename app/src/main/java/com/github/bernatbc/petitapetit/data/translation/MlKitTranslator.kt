package com.github.bernatbc.petitapetit.data.translation

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class MlKitTranslator : WordTranslator, AutoCloseable {

    private val client = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.FRENCH)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build(),
    )

    @Volatile
    private var modelReady = false

    init {
        downloadModel()
    }

    // Until the model is on the device, callers get an exception so that they can fall back.
    override suspend fun translate(text: String): String {
        if (!modelReady) {
            downloadModel()
            throw IllegalStateException("Translation model not downloaded yet")
        }
        return client.translate(text).await()
    }

    private fun downloadModel() {
        val task: Task<Void> = client.downloadModelIfNeeded(DownloadConditions.Builder().build())
        task.addOnSuccessListener { modelReady = true }
    }

    override fun close() = client.close()
}
