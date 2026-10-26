package com.github.bernatbc.petitapetit.data

data class Episode(
    val id: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val pageUrl: String,
    val publishedAtMillis: Long?,
)
