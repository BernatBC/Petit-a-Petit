package com.github.bernatbc.petitapetit.domain

object PromptBuilder {

    const val MIN_SUMMARY_WORDS = 30

    fun countWords(text: String): Int = text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }

    fun build(summary: String, topic: String = ""): String {
        val topicLine = topic.trim().takeIf { it.isNotEmpty() }
            ?.let { " L'actualité concerne : $it." }
            .orEmpty()
        return """
            Tu es mon professeur de français : bienveillant, patient et encourageant. Réponds toujours en français.

            Chaque jour, j'écoute le « Journal en français facile » de RFI, puis j'écris un résumé d'une actualité que j'ai entendue.$topicLine

            Voici mon résumé :

            « ${summary.trim()} »

            Procède en deux étapes.

            Étape 1 – Correction
            - Corrige mon résumé : liste mes erreurs (grammaire, conjugaison, vocabulaire, orthographe, syntaxe) avec une courte explication pour chacune.
            - Propose ensuite une version corrigée et naturelle de mon texte.
            - Termine par deux ou trois conseils pour progresser.

            Étape 2 – Discussion
            - Engage ensuite une conversation orale avec moi sur cette actualité.
            - Pose une seule question à la fois et attends ma réponse avant de continuer.
            - Garde un ton naturel et des phrases courtes, comme à l'oral.
            - Reformule brièvement mes erreurs importantes pendant la discussion, sans interrompre le fil de la conversation.
        """.trimIndent()
    }
}
