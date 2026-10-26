package com.github.bernatbc.petitapetit.domain

object WordLookup {

    private val elisionPrefixes =
        setOf("l", "d", "j", "m", "t", "s", "n", "c", "qu", "jusqu", "lorsqu", "puisqu")

    // Strips surrounding punctuation and a leading elided article/pronoun (l'attaque -> attaque).
    fun normalize(raw: String): String {
        val word = raw.trim { !it.isLetterOrDigit() }
        val apostrophe = word.indexOfFirst { it == '\'' || it == '’' }
        if (apostrophe > 0 && apostrophe < word.lastIndex &&
            word.substring(0, apostrophe).lowercase() in elisionPrefixes
        ) {
            return word.substring(apostrophe + 1)
        }
        return word
    }

    // Some services answer in capitals for lowercase input.
    fun matchCase(word: String, translation: String): String =
        if (translation == translation.uppercase() && word != word.uppercase()) {
            translation.lowercase()
        } else {
            translation
        }
}
